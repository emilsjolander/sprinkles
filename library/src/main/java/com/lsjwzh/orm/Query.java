package com.lsjwzh.orm;

import android.support.annotation.NonNull;

/**
 * Contains static methods to initiate queries
 */
public final class Query {
    private final Sprinkles sprinkles;

    /**
     * Start a query for a single instance of type T
     *
     * @param clazz   The class representing the type of the model you want returned
     * @param sql     The raw sql statement that should be executed.
     * @param sqlArgs The array of arguments to insert instead of ? in the placeholderQuery statement.
     *                Strings are automatically placeholderQuery escaped.
     * @param <T>     The type of the model you want returned
     * @return the query to execute
     */
    public static <T extends QueryResult> OneQuery<T> one(@NonNull Sprinkles sprinkles,
                                                          @NonNull Class<T> clazz,
                                                          @NonNull String sql,
                                                          Object... sqlArgs) {
        final OneQuery<T> query = new OneQuery<>(sprinkles);
        query.resultClass = clazz;
        query.placeholderQuery = sql;
        query.rawQuery = Utils.insertSqlArgs(sprinkles, sql, sqlArgs);
        return query;
    }

    /**
     * Start a query for a single instance of type T
     *
     * @param clazz    The class representing the type of the model you want returned
     * @param sqlResId The raw sql resource id that should be executed.
     * @param sqlArgs  The array of arguments to insert instead of ? in the placeholderQuery statement.
     *                 Strings are automatically placeholderQuery escaped.
     * @param <T>      The type of the model you want returned
     * @return the query to execute
     */
    public static <T extends QueryResult> OneQuery<T> one(@NonNull Sprinkles sprinkles,
                                                          @NonNull Class<T> clazz, int sqlResId,
                                                          Object... sqlArgs) {
        String sql = Utils.readRawText(sprinkles.mContext.getResources(), sqlResId);
        return one(sprinkles, clazz, sql, sqlArgs);
    }

    /**
     * Start a query for a list of instance of type T
     *
     * @param clazz   The class representing the type of the list you want returned
     * @param sql     The raw sql statement that should be executed.
     * @param sqlArgs The array of arguments to insert instead of ? in the placeholderQuery statement.
     *                Strings are automatically placeholderQuery escaped.
     * @param <T>     The type of the list you want returned
     * @return the query to execute
     */
    public static <T extends QueryResult> ManyQuery<T> many(@NonNull Sprinkles sprinkles,
                                                            @NonNull Class<T> clazz, String sql,
                                                            Object... sqlArgs) {
        final ManyQuery<T> query = new ManyQuery<>(sprinkles);
        query.resultClass = clazz;
        query.placeholderQuery = sql;
        query.rawQuery = Utils.insertSqlArgs(sprinkles, sql, sqlArgs);
        return query;
    }

    /**
     * Start a query for a list of instance of type T
     *
     * @param clazz    The class representing the type of the list you want returned
     * @param sqlResId The raw sql resource id that should be executed.
     * @param sqlArgs  The array of arguments to insert instead of ? in the placeholderQuery statement.
     *                 Strings are automatically placeholderQuery escaped.
     * @param <T>      The type of the list you want returned
     * @return the query to execute
     */
    public static <T extends QueryResult> ManyQuery<T> many(@NonNull Sprinkles sprinkles,
                                                            @NonNull Class<T> clazz, int sqlResId,
                                                            Object... sqlArgs) {
        String sql = Utils.readRawText(sprinkles.mContext.getResources(), sqlResId);
        return many(sprinkles, clazz, sql, sqlArgs);
    }

    /**
     * Start a query for the entire list of instance of type T
     *
     * @param clazz The class representing the type of the list you want returned
     * @param <T>   The type of the list you want returned
     * @return the query to execute
     */
    public static <T extends Model> ManyQuery<T> all(@NonNull Sprinkles sprinkles, @NonNull Class<T> clazz) {
        return many(sprinkles, clazz, "SELECT * FROM " + DataResolver.getTableName(clazz));
    }

    public Query(Sprinkles sprinkles) {
        this.sprinkles = sprinkles;
    }

    public <T extends Model> ModelList<T> find(@NonNull QueryBuilder<T> queryBuilder) {
        String sql = queryBuilder.build(sprinkles);
        CursorList<T> cursorList = Query.many(sprinkles, queryBuilder.modelClass, sql).get();
        ModelList<T> result = ModelList.from(cursorList, queryBuilder.skip);
        cursorList.close();
        return result;
    }

    public <T extends Model> T findSingle(@NonNull QueryBuilder<T> queryBuilder) {
        String sql = queryBuilder.build(sprinkles);
        if (!sql.contains("LIMIT")) {
            queryBuilder.take(1);
            sql = queryBuilder.build(sprinkles);
        }
        return Query.one(sprinkles, queryBuilder.modelClass, sql).get();
    }

//    boolean validateQuery(){
//        //validate  syntax of the sql string
////        new BigDecimal().toBigInteger()
//        return true;
//    }

//    public CalcResult max(String field){
//        return this;
//    }
//    public Query<T> min(String field,int value){
//        return this;
//    }
//    public Query<T> average(String field,int value){
//        return this;
//    }
//    public Query<T> sum(String field,int value){
//        return this;
//    }
//    public Query<T> count(String field,int value){
//        return this;
//    }

}
