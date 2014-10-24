package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import se.emilsjolander.sprinkles.annotations.AutoGen;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.Key;
import se.emilsjolander.sprinkles.annotations.ManyToOne;
import se.emilsjolander.sprinkles.annotations.OneToMany;
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
        boolean isKey;
        boolean isAutoIncrement;
        boolean isDynamic;

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
    public static class ManyToOneColumnField extends ColumnField{
        String manyColumn;
        String oneColumn;
        Class<? extends Model> oneModelClass;
    }
    public static class OneToManyColumnField extends ColumnField{
        String manyColumn;
        String oneColumn;
        Class manyModelClass;
    }

    static Map<Class<? extends QueryResult>, ModelInfo> cache = new HashMap<Class<? extends QueryResult>, ModelInfo>();

    String tableName;
    Set<ColumnField> columns = new HashSet<ColumnField>();
    List<ColumnField> keys = new ArrayList<ColumnField>();
    Set<OneToManyColumnField> oneToManyColumns = new HashSet<OneToManyColumnField>();
    Set<ManyToOneColumnField> manyToOneColumns = new HashSet<ManyToOneColumnField>();
    ColumnField autoIncrementField;
    boolean isTableChecked = false;

    private ModelInfo(){
        // hide contructor
    }

    public static ModelInfo from(Class<? extends QueryResult> clazz) {
        synchronized (clazz) {
            if (cache.containsKey(clazz)) {
                return cache.get(clazz);
            }
            ModelInfo info = new ModelInfo();

            final Field[] fields = Utils.getAllDeclaredFields(clazz, Object.class);
            boolean isAutoGenerateColumnNames = clazz.isAnnotationPresent(AutoGen.class);
            for (Field field : fields) {
                if (field.isAnnotationPresent(DynamicColumn.class)) {
                    ColumnField column = new ColumnField();
                    column.isDynamic = true;

                    column.name = field.getAnnotation(DynamicColumn.class).value();
                    column.sqlType = Sprinkles.sInstance.getTypeSerializer(field.getType()).getSqlType().name();
                    column.field = field;

                    if (!info.columns.add(column)) {
                        throw new DuplicateColumnException(column.name);
                    }

                } else if (isAutoGenerateColumnNames || field.isAnnotationPresent(Column.class)) {
                    ColumnField column = new ColumnField();
                    column.isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);
                    column.isKey = field.isAnnotationPresent(Key.class) || column.isAutoIncrement;

                    //check relationship
                    if (field.isAnnotationPresent(ManyToOne.class)) {
                        ManyToOneColumnField m2oColumn = new ManyToOneColumnField();
                        m2oColumn.name = field.getAnnotation(ManyToOne.class).manyColumn();
                        m2oColumn.sqlType = Sprinkles.sInstance.getTypeSerializer(Integer.class).getSqlType().name();
                        //many2one need to store the foreign key value
                        m2oColumn.field = field;
                        m2oColumn.manyColumn = field.getAnnotation(ManyToOne.class).manyColumn();
                        m2oColumn.oneColumn = field.getAnnotation(ManyToOne.class).oneColumn();
                        m2oColumn.oneModelClass = field.getAnnotation(ManyToOne.class).oneModelClass();

                        if (!info.manyToOneColumns.add(m2oColumn)) {
                            throw new DuplicateColumnException(column.name);
                        }
                    } else if (field.isAnnotationPresent(OneToMany.class)) {
                        OneToManyColumnField o2mColumn = new OneToManyColumnField();
                        o2mColumn.name = field.getAnnotation(OneToMany.class).oneColumn();
//                    o2mColumn.sqlType = Sprinkles.sInstance.getTypeSerializer().getSqlType().name();
                        //one2many field do not need  sqltype
                        o2mColumn.field = field;
                        o2mColumn.manyColumn = field.getAnnotation(OneToMany.class).manyColumn();
                        o2mColumn.oneColumn = field.getAnnotation(OneToMany.class).oneColumn();
                        o2mColumn.manyModelClass = field.getAnnotation(OneToMany.class).manyModelClass();

                        if (!info.oneToManyColumns.add(o2mColumn)) {
                            throw new DuplicateColumnException(column.name);
                        }
                    } else if (!field.isAnnotationPresent(Ignore.class)){
                        //if 'AutoGenerateColumnNames' property of table has been set to true,
                        //the field will be recognized as a column default
                        column.name = isAutoGenerateColumnNames ? field.getName() : field.getAnnotation(Column.class).value();
                        column.sqlType = Sprinkles.sInstance.getTypeSerializer(field.getType()).getSqlType().name();
                        column.field = field;

                        if (column.isAutoIncrement && !column.sqlType.equals(SqlType.INTEGER.name())) {
                            throw new AutoIncrementMustBeIntegerException(column.name);
                        }
                        if (column.isAutoIncrement) {
                            info.autoIncrementField = column;
                        }
                        if (column.isKey) {
                            info.keys.add(column);
                        }
                        if (!info.columns.add(column)) {
                            throw new DuplicateColumnException(column.name);
                        }
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
            }

            cache.put(clazz, info);
            return info;
        }
    }

    static void clearCache(){
        cache.clear();
    }

}
