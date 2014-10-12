package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.Index;
import se.emilsjolander.sprinkles.annotations.Table;
import se.emilsjolander.sprinkles.exceptions.NoTableAnnotationException;
import se.emilsjolander.sprinkles.typeserializers.TypeSerializer;

class Utils {

    static <T extends QueryResult> T getResultFromCursor(Class<T> resultClass, Cursor c) {
        try {
            final ModelInfo info = ModelInfo.from(resultClass);
            T result = Model.createModel(resultClass);
            List<String> colNames = Arrays.asList(c.getColumnNames());
            for (ModelInfo.ColumnField column : info.columns) {
                if (!colNames.contains(column.name)) {
                    continue;
                }
                column.field.setAccessible(true);
                final Class<?> type = column.field.getType();
                Object o = Sprinkles.sInstance.getTypeSerializer(type).unpack(c, column.name);
                column.field.set(result, o);
            }
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    static String getCreateTableSQL(ModelInfo table) {

        StringBuffer strIndexCreatorSQL = new StringBuffer();
        StringBuffer strSQL = new StringBuffer();
        strSQL.append("CREATE TABLE IF NOT EXISTS ");
        strSQL.append(table.tableName);
        strSQL.append(" ( ");

        /**
         * create primary key
         */
        for (ModelInfo.ColumnField keyField : table.keys) {
            Class<?> primaryClazz = keyField.field.getType();
            if (primaryClazz == int.class || primaryClazz == Integer.class
                    || primaryClazz == long.class || primaryClazz == Long.class) {
                if (keyField.isAutoIncrement) {
                    strSQL.append(keyField.name).append(" INTEGER PRIMARY KEY AUTOINCREMENT,");
                } else {
                    strSQL.append(keyField.name).append(" INTEGER PRIMARY KEY,");
                }
            } else {
                strSQL.append(keyField.name).append(" TEXT PRIMARY KEY,");
            }
        }

        Collection<ModelInfo.ColumnField> columns = table.columns;
        for (ModelInfo.ColumnField columnField : columns) {
            //skip key field
            if (columnField.isKey) {
                continue;
            }
            strSQL.append(columnField.name);
            Class<?> dataType = columnField.field.getType();
            if (dataType == int.class || dataType == Integer.class
                    || dataType == long.class || dataType == Long.class) {
                strSQL.append(" INTEGER");
            } else if (dataType == float.class || dataType == Float.class
                    || dataType == double.class || dataType == Double.class) {
                strSQL.append(" REAL");
            } else if (dataType == boolean.class || dataType == Boolean.class) {
                strSQL.append(" NUMERIC");
            }
            strSQL.append(",");
            //add by pwy 2013/11/4 for 创建索引
            if (columnField.field.getAnnotation(Index.class) != null) {
                strIndexCreatorSQL.append("create index ")
                        .append(columnField.name + "_idx on ")
                        .append(table.tableName + " (" + columnField.name + ");");
            }
        }

        Collection<ModelInfo.ManyToOneColumnField> manyToOnes = table.manyToOneColumns;
        for (ModelInfo.ManyToOneColumnField manyToOne : manyToOnes) {
            strSQL.append("\"").append(manyToOne.manyColumn);
            Class<?> dataType = manyToOne.field.getType();
            if (dataType == int.class || dataType == Integer.class
                    || dataType == long.class || dataType == Long.class) {
                strSQL.append("\"    ").append("INTEGER DEFAULT(0),");
            } else if (dataType == Double.class || dataType == double.class
                    || dataType == Float.class || dataType == float.class) {
                strSQL.append("\"    ").append("REAL DEFAULT(0),");
            } else {
                strSQL.append("\",");
            }
        }
        strSQL.deleteCharAt(strSQL.length() - 1);
        strSQL.append(" );").append(strIndexCreatorSQL.toString());
        return strSQL.toString();
    }

    static String getWhereStatement(Model m) {
        final ModelInfo info = ModelInfo.from(m.getClass());
        final StringBuilder where = new StringBuilder();
        final Object[] args = new Object[info.keys.size()];

        for (int i = 0; i < info.keys.size(); i++) {
            final ModelInfo.ColumnField column = info.keys.get(i);
            where.append(column.name);
            where.append("=?");

            column.field.setAccessible(true);
            try {
                args[i] = column.field.get(m);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }

            // split where statements with AND
            if (i < info.keys.size() - 1) {
                where.append(" AND ");
            }
        }

        return Utils.insertSqlArgs(where.toString(), args);
    }

    static ContentValues getContentValues(Model model) {
        final ModelInfo info = ModelInfo.from(model.getClass());
        final ContentValues values = new ContentValues();

        for (ModelInfo.ColumnField column : info.columns) {
            if (column.isAutoIncrement || column.isDynamic) {
                continue;
            }
            column.field.setAccessible(true);
            Object value;
            try {
                value = column.field.get(model);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            Sprinkles.sInstance.getTypeSerializer(column.field.getType()).pack(value, values, column.name);
        }
        //ToDo export foreign key value?

        return values;
    }

    static <T extends Model> Uri getNotificationUri(Class<T> clazz) {
        return Uri.parse("sprinkles://" + getTableName(clazz));
    }

    static String getTableName(Class<? extends Model> clazz) {
        if (clazz.isAnnotationPresent(Table.class)) {
            String tabName = clazz.getAnnotation(Table.class).value();
            return TextUtils.isEmpty(tabName) ? clazz.getName().replace(".","_") : tabName;
        }
        throw new NoTableAnnotationException();
    }

    static void assureTableExist(ModelInfo table) {
        synchronized (table) {
            if (!Utils.isTableExist(table)) {
                String sql = Utils.getCreateTableSQL(table);
                Sprinkles.getDatabase().execSQL(sql);
            }
        }
    }

    /**
     * check is table exist
     *
     * @param table
     * @return
     */
    static boolean isTableExist(ModelInfo table) {
        if (table.isTableChecked) {
            return true;
        }

        Cursor cursor = null;
        try {
            String sql = "SELECT COUNT(*) AS c FROM sqlite_master WHERE type ='table' AND name ='"
                    + table.tableName + "' ";
            cursor = Sprinkles.getDatabase().rawQuery(sql, null);
            if (cursor != null && cursor.moveToNext()) {
                int count = cursor.getInt(0);
                if (count > 0) {
                    table.isTableChecked = true;
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null)
                cursor.close();
            cursor = null;
        }

        return false;

    }

    static String insertSqlArgs(String sql, Object[] args) {
        if (args == null) {
            return sql;
        }
        for (Object o : args) {
            TypeSerializer typeSerializer = Sprinkles.sInstance.getTypeSerializer(o.getClass());
            String sqlObject = typeSerializer.toSql(o);
            sql = sql.replaceFirst("\\?", sqlObject);
        }
        return sql;
    }

    static Field[] getAllDeclaredFields(Class<?> clazz, Class<?> stopAt) {
        Field[] result = new Field[]{};
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

    static String readRawText(int rawId) {
        final InputStream inputStream = Sprinkles.sInstance.mContext
                .getResources().openRawResource(rawId);
        final InputStreamReader inputStreamReader = new InputStreamReader(
                inputStream);
        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        String line;
        final StringBuilder body = new StringBuilder();
        try {
            while ((line = bufferedReader.readLine()) != null) {
                body.append(line);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }
}
