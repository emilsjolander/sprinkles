package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.support.annotation.NonNull;

import se.emilsjolander.sprinkles.annotations.AutoGen;
import se.emilsjolander.sprinkles.annotations.AutoIncrement;
import se.emilsjolander.sprinkles.annotations.Column;
import se.emilsjolander.sprinkles.annotations.DynamicColumn;
import se.emilsjolander.sprinkles.annotations.Ignore;
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
        @Override
        public boolean equals(Object o) {
            if (o instanceof ManyToOneColumnField) {
                ManyToOneColumnField columnField = (ManyToOneColumnField) o;
                return columnField.manyColumn.equals(manyColumn)
                        && columnField.oneColumn.equals(oneColumn)
                        && columnField.oneModelClass.equals(oneModelClass);
            }
            return false;
        }
    }
    public static class OneToManyColumnField extends ColumnField{
        String manyColumn;
        String oneColumn;
        Class manyModelClass;
        @Override
        public boolean equals(Object o) {
            if (o instanceof OneToManyColumnField) {
                OneToManyColumnField columnField = (OneToManyColumnField) o;
                return columnField.manyColumn.equals(manyColumn)
                        && columnField.oneColumn.equals(oneColumn)
                        && columnField.manyModelClass.equals(manyModelClass);
            }
            return false;
        }
    }

    String tableName;
    Set<ColumnField> columns = new HashSet<ColumnField>();
    List<ColumnField> keys = new ArrayList<ColumnField>();
    Set<OneToManyColumnField> oneToManyColumns = new HashSet<OneToManyColumnField>();
    Set<ManyToOneColumnField> manyToOneColumns = new HashSet<ManyToOneColumnField>();
    ColumnField autoIncrementField;
    boolean isTableChecked = false;
    final Sprinkles sprinkles;

    private ModelInfo(Sprinkles sprinkles){
        // hide contructor
        this.sprinkles = sprinkles;
    }

  static ModelInfo from(@NonNull Sprinkles sprinkles, @NonNull Class<? extends QueryResult> clazz) {
        synchronized (clazz) {
            if (sprinkles.modelInfoCache.containsKey(clazz)) {
                return sprinkles.modelInfoCache.get(clazz);
            }
            ModelInfo info = new ModelInfo(sprinkles);

            final Field[] fields = Utils.getAllDeclaredFields(clazz, Object.class);
            boolean isAutoGenerateColumnNames = clazz.isAnnotationPresent(AutoGen.class);
            for (Field field : fields) {
                if (field.isAnnotationPresent(DynamicColumn.class)) {
                    ColumnField column = new ColumnField();
                    column.isDynamic = true;

                    column.name = field.getAnnotation(DynamicColumn.class).value();
                    column.sqlType = sprinkles.getTypeSerializer(field.getType()).getSqlType().name();
                    column.field = field;

                    if (!info.columns.add(column)) {
                        throw new DuplicateColumnException(column.name);
                    }

                } else if (isAutoGenerateColumnNames || field.isAnnotationPresent(Column.class)) {
                    //check relationship
                    if (field.isAnnotationPresent(ManyToOne.class)) {
                        ManyToOneColumnField m2oColumn = new ManyToOneColumnField();
                        m2oColumn.name = field.getAnnotation(ManyToOne.class).manyColumn();
                        m2oColumn.sqlType = sprinkles.getTypeSerializer(Integer.class).getSqlType().name();
                        //many2one need to store the foreign key value
                        m2oColumn.field = field;
                        m2oColumn.manyColumn = field.getAnnotation(ManyToOne.class).manyColumn();
                        m2oColumn.oneColumn = field.getAnnotation(ManyToOne.class).oneColumn();
                        if(field.getType()!=field.getGenericType()){
                            m2oColumn.oneModelClass = (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];
                        }else{
                            m2oColumn.oneModelClass = (Class)field.getType();
                        }

                        if (!info.manyToOneColumns.add(m2oColumn)) {
                            throw new DuplicateColumnException(m2oColumn.name);
                        }
                    } else if (field.isAnnotationPresent(OneToMany.class)) {
                        OneToManyColumnField o2mColumn = new OneToManyColumnField();
                        o2mColumn.name = field.getAnnotation(OneToMany.class).oneColumn();
//                    o2mColumn.sqlType = Sprinkles.sInstance.getTypeSerializer().getSqlType().name();
                        //one2many field do not need  sqltype
                        o2mColumn.field = field;
                        o2mColumn.manyColumn = field.getAnnotation(OneToMany.class).manyColumn();
                        o2mColumn.oneColumn = field.getAnnotation(OneToMany.class).oneColumn();
                        o2mColumn.manyModelClass = (Class) ((ParameterizedType)field.getGenericType()).getActualTypeArguments()[0];//field.getAnnotation(OneToMany.class).manyModelClass();

                        if (!info.oneToManyColumns.add(o2mColumn)) {
                            throw new DuplicateColumnException(o2mColumn.name);
                        }
                    } else if (!field.isAnnotationPresent(Ignore.class)){
                        ColumnField column = new ColumnField();
                        column.isAutoIncrement = field.isAnnotationPresent(AutoIncrement.class);
                        column.isKey = field.isAnnotationPresent(Key.class) || column.isAutoIncrement;

                        //if 'AutoGenerateColumnNames' property of table has been set to true,
                        //the field will be recognized as a column default
                        column.name = isAutoGenerateColumnNames ? field.getName() : field.getAnnotation(Column.class).value();
                        column.sqlType = sprinkles.getTypeSerializer(field.getType()).getSqlType().name();
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
                info.tableName = DataResolver.getTableName((Class<? extends Model>) clazz);
                if (info.keys.size() == 0) {
                    throw new NoKeysException();
                }
            }

            sprinkles.modelInfoCache.put(clazz, info);
            return info;
        }
    }

}
