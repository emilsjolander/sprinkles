package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;

class Utils {

	static <T extends QueryResult> T getResultFromCursor(Class<T> resultClass, Cursor c) {
		try {
			T result = resultClass.newInstance();
			final Set<ColumnField> columns = getColumns(resultClass);
			final Set<ColumnField> dynamicColumns = getDynamicColumns(resultClass);
			for (ColumnField cf : dynamicColumns) {
				if (!columns.add(cf)) {
					throw new DuplicateColumnException(cf.name);
				}
			}
			List<String> colNames = Arrays.asList(c.getColumnNames());
			for (ColumnField column : columns) {
				if (!colNames.contains(column.name)) {
					continue;
				}
				column.field.setAccessible(true);
				final Class<?> type = column.field.getType();
				Object o = Sprinkles.sInstance.typeSerializers.get(type).unpack(c, column.name);
				column.field.set(result, o);
			}
			return result;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	static String getWhereStatement(Model m) {
		final Iterable<ColumnField> columns = getColumns(m.getClass());
		final StringBuilder where = new StringBuilder();
		final List<Object> args = new ArrayList<Object>();

		boolean notFirstPK = false;
		for (ColumnField column : columns) {
			if (column.isPrimaryKey || column.isAutoIncrementPrimaryKey) {

				// Split the where statement by putting an AND before every clause except the first
				if (notFirstPK) {
					where.append(" AND ");
				}
				notFirstPK = true;
				where.append(column.name);
				where.append("=?");

				column.field.setAccessible(true);
				try {
					args.add(column.field.get(m));
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return Utils.insertSqlArgs(where.toString(), args.toArray());
	}

	static ContentValues getContentValues(Model model) {
		final Iterable<ColumnField> columns = getColumns(model.getClass());
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
				Sprinkles.sInstance.typeSerializers.get(value.getClass()).pack(value, values, column.name);
			}
		}

		return values;
	}

	// TODO this method does way to much to be called on every save and query. Cache results!
	static Set<ColumnField> getColumns(Class<? extends QueryResult> clazz) {
		final Field[] fields = getAllDeclaredFields(clazz, Object.class);
		final Set<ColumnField> columns = new HashSet<ColumnField>();

		for (Field field : fields) {
			if (field.isAnnotationPresent(Column.class)) {
				ColumnField column = new ColumnField();
				column.name = field.getAnnotation(Column.class).value();

				if (!columns.add(column)) {
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

				column.type = Sprinkles.sInstance.typeSerializers.get(field.getType()).getSqlType().name();

				if (column.isAutoIncrementPrimaryKey && !column.type.equals("INTEGER")) {
					throw new AutoIncrementMustBeIntegerException(column.name);
				}

				if (column.isAutoIncrementPrimaryKey && (column.isPrimaryKey || column.isForeignKey)) {
					throw new IllegalStateException("A @AutoIncrementPrimaryKey field may not also be an @PrimaryKey or @ForeignKey field");
				}

				column.field = field;
			}
		}

		if (columns.isEmpty()) {
			throw new EmptyTableException(clazz.getName());
		}

		int numberOfAutoIncrementFields = 0;
		int numberOfPrimaryKeys = 0;
		for (ColumnField column : columns) {
			if (column.isAutoIncrementPrimaryKey) {
				numberOfAutoIncrementFields++;
				if (numberOfAutoIncrementFields > 1) {
					throw new MultipleAutoIncrementFieldsException();
				}
			} else if (column.isPrimaryKey) {
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

	static Set<ColumnField> getDynamicColumns(Class<? extends QueryResult> clazz) {
		final Field[] fields = getAllDeclaredFields(clazz, Object.class);
		final Set<ColumnField> columns = new HashSet<ColumnField>();

		for (Field field : fields) {
			if (field.isAnnotationPresent(DynamicColumn.class)) {
				ColumnField column = new ColumnField();
				column.name = field.getAnnotation(DynamicColumn.class).value();

				if (!columns.add(column)) {
					throw new DuplicateColumnException(column.name);
				}

				column.type = Sprinkles.sInstance.typeSerializers.get(field.getType()).getSqlType().name();
				column.field = field;
			}
		}
		return columns;
	}

	static <T extends Model> Uri getNotificationUri(Class<T> clazz) {
		return Uri.parse("sprinkles://" + getTableName(clazz));
	}

    /**
     * Returns the name of the table in which a {@link se.emilsjolander.sprinkles.Model} is stored.
     * @param clazz of a Model for which the name of the table is desired
     * @return the name of the table for the specified Model
     */
	public static String getTableName(Class<? extends Model> clazz) {
		Table table = clazz.getAnnotation(Table.class);
		if (table != null) {
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

	static Field[] getAllDeclaredFields(Class<?> clazz, Class<?> stopAt) {
		Field[] result = new Field[] {};
		while (!clazz.equals(stopAt)) {
			result = concatArrays(result, clazz.getDeclaredFields());
			clazz = clazz.getSuperclass();
		}
		return result;
	}

	static <T> T[] concatArrays(T[] one, T[] two) {
		if (one == null || one.length == 0) {
			return two;
		} else if (two == null || two.length == 0) {
			return one;
		}
		final int length = one.length + two.length;
		@SuppressWarnings("unchecked") // cast is safe b/c of getComponentType
		final T[] result = (T[]) Array.newInstance(one.getClass().getComponentType(), length);
		System.arraycopy(one, 0, result, 0, one.length);
		System.arraycopy(two, 0, result, one.length, two.length);
		return result;
	}
}
