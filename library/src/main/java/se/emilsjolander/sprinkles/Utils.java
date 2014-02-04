package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

class Utils {

	static <T extends QueryResult> T getResultFromCursor(Class<T> resultClass, Cursor c) {
		try {
            final ModelInfo info = ModelInfo.from(resultClass);
			T result = resultClass.newInstance();
            List<String> colNames = Arrays.asList(c.getColumnNames());
            for (ModelInfo.ColumnField column : info.columns) {
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
        final ModelInfo info = ModelInfo.from(m.getClass());
		final StringBuilder where = new StringBuilder();
        final Object[] args = new Object[info.primaryKeys.size()];

		for (int i = 0; i < info.primaryKeys.size(); i++) {
			final ModelInfo.StaticColumnField column = info.primaryKeys.get(i);
			where.append(column.name);
			where.append("=?");

            column.field.setAccessible(true);
            try {
                args[i] = column.field.get(m);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // split where statements with AND
			if (i < info.primaryKeys.size()-1) {
				where.append(" AND ");
			}
		}

		return Utils.insertSqlArgs(where.toString(), args);
	}

	static ContentValues getContentValues(Model model) {
		final ModelInfo info = ModelInfo.from(model.getClass());
		final ContentValues values = new ContentValues();

		for (ModelInfo.StaticColumnField column : info.staticColumns) {
			if (column.isAutoIncrement) {
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

    static <T extends Model> Uri getNotificationUri(Class<T> clazz) {
        return Uri.parse("sprinkles://" + ModelInfo.from(clazz).tableName);
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

}
