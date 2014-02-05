package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.sprinkles.annotations.AutoIncrementPrimaryKey;
import se.emilsjolander.sprinkles.annotations.CascadeDelete;
import se.emilsjolander.sprinkles.annotations.Check;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.ConflictClause;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.ForeignKey;
import se.emilsjolander.sprinkles.annotations.NotNull;
import se.emilsjolander.sprinkles.annotations.PrimaryKey;
import se.emilsjolander.sprinkles.annotations.Unique;
import se.emilsjolander.sprinkles.exceptions.AutoIncrementMustBeIntegerException;
import se.emilsjolander.sprinkles.exceptions.CannotCascadeDeleteNonForeignKey;
import se.emilsjolander.sprinkles.exceptions.DuplicateColumnException;
import se.emilsjolander.sprinkles.exceptions.EmptyTableException;
import se.emilsjolander.sprinkles.exceptions.NoPrimaryKeysException;
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

        boolean isPrimaryKey;
        boolean isAutoIncrement;
        boolean isCascadeDelete;
        boolean isNotNull;

        boolean isForeignKey;
        String foreignKey;

        boolean isUnique;
        ConflictClause uniqueConflictClause;

        boolean hasCheck;
        String checkClause;
    }

    public static class DynamicColumnField extends ColumnField {

    }

    private static Map<Class<? extends QueryResult>, ModelInfo> cache = new HashMap<Class<? extends QueryResult>, ModelInfo>();

    String tableName;
    Set<ColumnField> columns = new HashSet<ColumnField>();
    List<DynamicColumnField> dynamicColumns = new ArrayList<DynamicColumnField>();
    List<StaticColumnField> staticColumns = new ArrayList<StaticColumnField>();
    List<StaticColumnField> foreignKeys = new ArrayList<StaticColumnField>();
    List<StaticColumnField> primaryKeys = new ArrayList<StaticColumnField>();
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

                column.isAutoIncrement = field.isAnnotationPresent(AutoIncrementPrimaryKey.class);
                column.isForeignKey = field.isAnnotationPresent(ForeignKey.class);
                column.isPrimaryKey = field.isAnnotationPresent(PrimaryKey.class) || column.isAutoIncrement;
                column.isCascadeDelete = field.isAnnotationPresent(CascadeDelete.class);
                column.isUnique = field.isAnnotationPresent(Unique.class);
                column.isNotNull = field.isAnnotationPresent(NotNull.class);
                column.hasCheck = field.isAnnotationPresent(Check.class);

                if (column.isForeignKey) {
                    column.foreignKey = field.getAnnotation(ForeignKey.class).value();
                } else if (column.isCascadeDelete) {
                    throw new CannotCascadeDeleteNonForeignKey();
                }

                column.sqlType = Sprinkles.sInstance.getTypeSerializer(field.getType()).getSqlType().name();
                column.field = field;

                if (column.isAutoIncrement && !column.sqlType.equals(SqlType.INTEGER.name())) {
                    throw new AutoIncrementMustBeIntegerException(column.name);
                }
                if (column.isAutoIncrement && column.isForeignKey) {
                    throw new IllegalStateException("A @AutoIncrementPrimaryKey field may not also be an @PrimaryKey or @ForeignKey field");
                }
                if (column.isAutoIncrement) {
                    info.autoIncrementColumn = column;
                }
                if (column.isForeignKey) {
                    info.foreignKeys.add(column);
                }
                if (column.isPrimaryKey) {
                    info.primaryKeys.add(column);
                }
                if (column.isUnique) {
                    column.uniqueConflictClause = field.getAnnotation(Unique.class).value();
                }
                if (column.hasCheck) {
                    column.checkClause = field.getAnnotation(Check.class).value();
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
            if (info.primaryKeys.size() == 0) {
                throw new NoPrimaryKeysException();
            }
            if (info.autoIncrementColumn != null && info.primaryKeys.size() > 1) {
                throw new IllegalStateException("A model with a field marked as @AutoIncrementPrimaryKey may not mark any other field with @PrimaryKey");
            }
        }

        cache.put(clazz, info);
        return info;
    }

}
