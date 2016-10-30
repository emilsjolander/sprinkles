package se.emilsjolander.sprinkles;

import android.content.ContentValues;
import android.content.res.Resources;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.sprinkles.annotations.Index;
import se.emilsjolander.sprinkles.typeserializers.TypeSerializer;

class Utils {


    static String getCreateTableSQL(ModelInfo table) {

        StringBuffer strIndexCreatorSQL = new StringBuffer();
        StringBuffer strSQL = new StringBuffer();
        Set<String> fieldNames = new HashSet<String>();

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
            fieldNames.add(keyField.name);
        }

        Collection<ModelInfo.ColumnField> columns = table.columns;
        for (ModelInfo.ColumnField columnField : columns) {
            //skip key field
            if (columnField.isKey) {
                continue;
            }
            fieldNames.add(columnField.name);
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
            if (fieldNames.contains(manyToOne.manyColumn)) {
                continue;
            }
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

    static String getWhereStatement(@NonNull Sprinkles sprinkles, @NonNull Model m) {
        final ModelInfo info = ModelInfo.from(sprinkles, m.getClass());
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

        return Utils.insertSqlArgs(sprinkles, where.toString(), args);
    }


    static ContentValues getContentValues(@NonNull Sprinkles sprinkles, Model model) {
        final ModelInfo info = ModelInfo.from(sprinkles, model.getClass());
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
            sprinkles.getTypeSerializer(column.field.getType()).pack(value, values, column.name);
        }
        // export foreign key value
        for (ModelInfo.ManyToOneColumnField manyToOneColumnField : info.manyToOneColumns) {
            //skip lazy load field
            if (LazyModelList.class.isAssignableFrom(manyToOneColumnField.field.getType())) {
                continue;
            }
            manyToOneColumnField.field.setAccessible(true);
            Object oneModel;
            try {
                oneModel = manyToOneColumnField.field.get(model);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (oneModel != null) {
                //fetch oneModel's key value
                Field fieldInOneMdel = null;
                try {
                    fieldInOneMdel = oneModel.getClass().getDeclaredField(manyToOneColumnField.oneColumn);
                    fieldInOneMdel.setAccessible(true);
                    Object foreignKeyValue = fieldInOneMdel.get(oneModel);
                    sprinkles.getTypeSerializer(fieldInOneMdel.getType()).pack(foreignKeyValue, values, manyToOneColumnField.manyColumn);
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        //export hiddenFields
        for (Map.Entry<String, Object> hiddenField : model.mHiddenFieldsMap.entrySet()) {
            if (hiddenField.getValue() == null) {
                values.putNull(hiddenField.getKey());
            } else {
                sprinkles.getTypeSerializer(hiddenField.getValue().getClass()).pack(hiddenField.getValue(), values, hiddenField.getKey());
            }
        }

        return values;
    }

    static <T extends Model> Uri getNotificationUri(@NonNull Class<T> clazz) {
        return Uri.parse("sprinkles://" + DataResolver.getTableName(clazz));
    }

    static String insertSqlArgs(Sprinkles sprinkles, String sql, Object[] args) {
        if (args == null) {
            return sql;
        }
        for (Object o : args) {
            TypeSerializer typeSerializer = sprinkles.getTypeSerializer(o.getClass());
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

    static String readRawText(Resources resources, int rawId) {
        final InputStream inputStream = resources.openRawResource(rawId);
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
