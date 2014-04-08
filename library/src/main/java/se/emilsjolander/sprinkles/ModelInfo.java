package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.sprinkles.annotations.AutoIncrementKey;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.exceptions.AutoIncrementMustBeIntegerException;
import se.emilsjolander.sprinkles.exceptions.DuplicateColumnException;
import se.emilsjolander.sprinkles.exceptions.EmptyTableException;
import se.emilsjolander.sprinkles.exceptions.NoKeysException;
import se.emilsjolander.sprinkles.typeserializers.SqlType;

class ModelInfo {

    public static class ColumnField {
        String name;
        String sqlType;
        Field field;

        @Override
        public boolean equals(Object o) {
            if (o instanceof ColumnField) {
                return ((ColumnField) o).name.equals(name);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.name.hashCode();
        }
    }

    public static class StaticColumnField extends ColumnField {
        boolean isKey;
        boolean isAutoIncrement;
    }

    public static class DynamicColumnField extends ColumnField {

    }

    private static Map<Class<? extends QueryResult>, ModelInfo> cache = new HashMap<Class<? extends QueryResult>, ModelInfo>();

    String tableName;
    Set<ColumnField> columns = new HashSet<ColumnField>();
    List<DynamicColumnField> dynamicColumns = new ArrayList<DynamicColumnField>();
    List<StaticColumnField> staticColumns = new ArrayList<StaticColumnField>();
    List<StaticColumnField> keys = new ArrayList<StaticColumnField>();
    StaticColumnField autoIncrementColumn;

    private ModelInfo(){
        // hide contructor
    }

    static ModelInfo from(Class<? extends QueryResult> clazz) {
        if (cache.containsKey(clazz)) {
            return cache.get(clazz);
        }
        ModelInfo info = new ModelInfo();

        final Field[] fields = Utils.getAllDeclaredFields(clazz, Object.class);
        for (Field field : fields) {
            if (field.isAnnotationPresent(DynamicColumn.class)) {
                DynamicColumnField column = new DynamicColumnField();
                column.name = field.getAnnotation(DynamicColumn.class).value();
                column.sqlType = Sprinkles.sInstance.getTypeSerializer(field.getType()).getSqlType().name();
                column.field = field;
                info.dynamicColumns.add(column);
                if (!info.columns.add(column)) {
                    throw new DuplicateColumnException(column.name);
                }

            } else if (field.isAnnotationPresent(Column.class)) {
                StaticColumnField column = new StaticColumnField();
                column.name = field.getAnnotation(Column.class).value();

                column.isAutoIncrement = field.isAnnotationPresent(AutoIncrementKey.class);
                column.isKey = field.isAnnotationPresent(Key.class) || column.isAutoIncrement;

                column.sqlType = Sprinkles.sInstance.getTypeSerializer(field.getType()).getSqlType().name();
                column.field = field;

                if (column.isAutoIncrement && !column.sqlType.equals(SqlType.INTEGER.name())) {
                    throw new AutoIncrementMustBeIntegerException(column.name);
                }
                if (column.isAutoIncrement) {
                    info.autoIncrementColumn = column;
                }
                if (column.isKey) {
                    info.keys.add(column);
                }

                info.staticColumns.add(column);
                if (!info.columns.add(column)) {
                    throw new DuplicateColumnException(column.name);
                }
            }
        }

        if (info.columns.isEmpty()) {
            throw new EmptyTableException(clazz.getName());
        }
        if (Model.class.isAssignableFrom(clazz)) {
            info.tableName = Utils.getTableName((Class<? extends Model>) clazz);
            if (info.keys.size() == 0) {
                throw new NoKeysException();
            }
            if (info.autoIncrementColumn != null && info.keys.size() > 1) {
                throw new IllegalStateException("A model with a field marked as @AutoIncrementKey may not mark any other field with @Key");
            }
        }

        cache.put(clazz, info);
        return info;
    }

}
