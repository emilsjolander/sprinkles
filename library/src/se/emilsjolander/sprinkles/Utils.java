package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.CascadeDelete;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.ForeignKey;
import se.emilsjolander.sprinkles.annotations.PrimaryKey;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.exceptions.AutoIncrementMustBeIntegerException;
import se.emilsjolander.sprinkles.exceptions.CannotCascadeDeleteNonForeignKey;
import se.emilsjolander.sprinkles.exceptions.DuplicateColumnException;
import se.emilsjolander.sprinkles.exceptions.EmptyTableException;
import se.emilsjolander.sprinkles.exceptions.MultipleAutoIncrementFieldsException;
import se.emilsjolander.sprinkles.exceptions.NoPrimaryKeysException;
import se.emilsjolander.sprinkles.exceptions.NoSuchSqlTypeExistsException;
import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;

class Utils {
	
	static <T extends Model> Uri getNotificationUri(Class<T> resultClass) {
		return Uri.parse("sprinkles://"+resultClass.getAnnotation(Table.class).value());
	}
	
	static <T extends QueryResult> T getResultFromCursor(Class<T> resultClass, Cursor c) {
		try {
			T result = resultClass.newInstance();
			final List<ColumnField> columns = getColumns(resultClass);
            final List<ColumnField> dynamicColumns = getDynamicColumns(resultClass);
            for (ColumnField cf : dynamicColumns) {
                if (columns.contains(cf)) {
                    throw new DuplicateColumnException(cf.name);
                } else {
                    columns.add(cf);
                }
            }
            List<String> colNames = Arrays.asList(c.getColumnNames());
            for (ColumnField column : columns) {
                if (!colNames.contains(column.name)) {
                    continue;
                }
				column.field.setAccessible(true);
				final Class<?> type = column.field.getType();
				if (isTypeOneOf(type, String.class)) {
					column.field.set(result, c.getString(c.getColumnIndexOrThrow(column.name)));
				} else if (isTypeOneOf(type, Integer.class, int.class)) {
					column.field.set(result, c.getInt(c.getColumnIndexOrThrow(column.name)));
				} else if (isTypeOneOf(type, Long.class, long.class)) {
					column.field.set(result, c.getLong(c.getColumnIndexOrThrow(column.name)));
				} else if (isTypeOneOf(type, Boolean.class, boolean.class)) {
					column.field.set(result, c.getInt(c.getColumnIndexOrThrow(column.name)) > 0);
				} else if (isTypeOneOf(type, Float.class, float.class)) {
					column.field.set(result, c.getFloat(c.getColumnIndexOrThrow(column.name)));
				} else if (isTypeOneOf(type, Double.class, double.class)) {
					column.field.set(result, c.getDouble(c.getColumnIndexOrThrow(column.name)));
				}
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String getTableName(Class<? extends Model> clazz) {
		if (clazz.isAnnotationPresent(Table.class)) {
			Table table = clazz.getAnnotation(Table.class);
			return table.value();
		}
		throw new NoTableAnnotationException();
	}

	static String insertSqlArgs(String sql, Object[] args) {
		if (args == null) {
			return sql;
		}
		for (Object o : args) {
			if (o instanceof Number) {
				sql = sql.replaceFirst("\\?", o.toString());
			} else {
				String escapedString = DatabaseUtils.sqlEscapeString(o.toString());
				sql = sql.replaceFirst("\\?", escapedString);
			}
		}
		return sql;
	}
	
	static String getWhereStatement(Model m) {
		final List<ColumnField> columns = Utils.getColumns(m.getClass());
		final List<ColumnField> primaryColumn = new ArrayList<ColumnField>();
		for (ColumnField column : columns) {
			if (column.isPrimaryKey || column.isAutoIncrementPrimaryKey) {
				primaryColumn.add(column);
			}
		}
		final StringBuilder where = new StringBuilder();
		for (int i = 0; i < primaryColumn.size(); i++) {
			final ColumnField column = primaryColumn.get(i);
			where.append(column.name);
			where.append("=?");
			
			// split where statements with AND
			if (i < primaryColumn.size()-1) {
				where.append(" AND ");
			}
		}
		final Object[] args = new Object[primaryColumn.size()];
		for (int i = 0; i < primaryColumn.size(); i++) {
			final ColumnField column = primaryColumn.get(i);
			column.field.setAccessible(true);
			try {
				args[i] = column.field.get(m);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		return Utils.insertSqlArgs(where.toString(), args);
	}

	static ContentValues getContentValues(Model model) {
		final List<ColumnField> columns = getColumns(model.getClass());
		final ContentValues values = new ContentValues();
		
		for (ColumnField column : columns) {
			if (column.isAutoIncrementPrimaryKey) {
				continue;
			}
			column.field.setAccessible(true);
			Object value;
			try {
				value = column.field.get(model);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			if (value != null) {
				if (value instanceof String) {
					values.put(column.name, (String) value);
				} else if (value instanceof Integer) {
					values.put(column.name, (Integer) value);
				} else if (value instanceof Long) {
					values.put(column.name, (Long) value);
				} else if (value instanceof Boolean) {
					values.put(column.name, (Boolean) value);
				} else if (value instanceof Float) {
					values.put(column.name, (Float) value);
				} else if (value instanceof Double) {
					values.put(column.name, (Double) value);
				}
			}
		}
		
		return values;
	}

    // TODO this method does way to much to be called on every save and query. Cache results!
	static List<ColumnField> getColumns(Class<? extends QueryResult> clazz) {
		final Field[] fields = getAllDeclaredFields(clazz, Object.class);
		final List<ColumnField> columns = new ArrayList<ColumnField>();

		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				ColumnField column = new ColumnField();
				column.name = field.getAnnotation(Column.class).value();

				if (columns.contains(column)) {
					throw new DuplicateColumnException(column.name);
				}

				column.isAutoIncrementPrimaryKey = field.isAnnotationPresent(AutoIncrementPrimaryKey.class);
				column.isForeignKey = field.isAnnotationPresent(ForeignKey.class);
				column.isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class);
				column.isCascadeDelete = field.isAnnotationPresent(CascadeDelete.class);

				if (column.isForeignKey) {
					column.foreignKey = field.getAnnotation(ForeignKey.class).value();
				} else if (column.isCascadeDelete) {
					throw new CannotCascadeDeleteNonForeignKey();
				}

				column.type = getSqlType(field);
				
				if (column.isAutoIncrementPrimaryKey && !column.type.equals("INTEGER")) {
					throw new AutoIncrementMustBeIntegerException(column.name);
				}
				
				if (column.isAutoIncrementPrimaryKey && (column.isPrimaryKey || column.isForeignKey)) {
					throw new IllegalStateException("A @AutoIncrementPrimaryKey field may not also be an @PrimaryKey or @ForeignKey field");
				}

				column.field = field;
				columns.add(column);
			}
		}

		if (columns.isEmpty()) {
			throw new EmptyTableException(clazz.getName());
		}

		int numberOfAutoIncrementFields = 0;
		for (ColumnField column : columns) {
			if (column.isAutoIncrementPrimaryKey) {
				numberOfAutoIncrementFields++;
				if (numberOfAutoIncrementFields > 1) {
					throw new MultipleAutoIncrementFieldsException();
				}
			}
		}

		int numberOfPrimaryKeys = 0;
		for (ColumnField column : columns) {
			if (column.isPrimaryKey) {
				numberOfPrimaryKeys++;
			}
		}
		
		if (numberOfAutoIncrementFields > 0 && numberOfPrimaryKeys > 0) {
			throw new IllegalStateException("A model with a field marked as @AutoIncrementPrimaryKey may not mark any other field with @PrimaryKey");
		}
		
		if (numberOfAutoIncrementFields == 0 && numberOfPrimaryKeys == 0) {
			throw new NoPrimaryKeysException();
		}

		return columns;
	}

    private static List<ColumnField> getDynamicColumns(Class<? extends QueryResult> clazz) {
        final Field[] fields = getAllDeclaredFields(clazz, Object.class);
        final List<ColumnField> columns = new ArrayList<ColumnField>();

        for (Field field : fields) {
            if (field.isAnnotationPresent(DynamicColumn.class)) {
                ColumnField column = new ColumnField();
                column.name = field.getAnnotation(DynamicColumn.class).value();

                if (columns.contains(column)) {
                    throw new DuplicateColumnException(column.name);
                }

                column.type = getSqlType(field);
                column.field = field;
                columns.add(column);
            }
        }
        return columns;
    }

	private static Field[] getAllDeclaredFields(Class<?> clazz, Class<?> stopAt) {
		Field[] result = new Field[] {};
		while (!clazz.equals(stopAt)) {
			result = concatArrays(result, clazz.getDeclaredFields());
			clazz = clazz.getSuperclass();
		}
		return result;
	}

    static <T> T[] concatArrays(T[] one, T[] two) {
        if (one == null) {
            return two;
        } else if (two == null) {
            return one;
        }
        final int length = one.length + two.length;
        final T[] result = (T[]) Array.newInstance(one.getClass().getComponentType(), length);
        for (int i = 0; i < length; i++) {
            if (i < one.length) {
                result[i] = one[i];
            } else {
                result[i] = two[i - one.length];
            }
        }
        return result;
    }

	private static String getSqlType(Field field) {
		Class<?> type = field.getType();
		if (isTypeOneOf(type, int.class, long.class, boolean.class, Integer.class, Long.class, Boolean.class)) {
			return "INTEGER";
		} else if(isTypeOneOf(type, float.class, double.class, Float.class, Double.class)) {
			return "REAL";
		} else if(isTypeOneOf(type, String.class)) {
			return "TEXT";
		}
		throw new NoSuchSqlTypeExistsException(field.getName());
	}

	private static boolean isTypeOneOf(Class<?> type, Class<?>... types) {
		for (Class<?> t : types) {
			if (type.equals(t)) {
				return true;
			}
		}
		return false;
	}

}
