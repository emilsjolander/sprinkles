package se.emilsjolander.sprinkles;

import java.math.BigDecimal;
import java.util.LinkedList;

/**
 * Contains static methods to initiate queries
 */
public final class Query<T extends Model> implements IQueryPart1<T>,IQueryPart2<T>,IQueryPart3<T>,IQueryPart4<T> {
    public enum SortOrder{
        ASC,DESC
    }
    /**
     * Start a query for a single instance of type T
     *
     * @param clazz
     *      The class representing the type of the model you want returned
     *
     * @param sql
     *      The raw sql statement that should be executed.
     *
     * @param sqlArgs
     *      The array of arguments to insert instead of ? in the placeholderQuery statement.
     *      Strings are automatically placeholderQuery escaped.
     *
     * @param <T>
     *      The type of the model you want returned
     *
     * @return the query to execute
     */
	public static <T extends QueryResult> OneQuery<T> one(Class<T> clazz, String sql,
			Object... sqlArgs) {
        final OneQuery<T> query = new OneQuery<T>();
		query.resultClass = clazz;
        query.placeholderQuery = sql;
		query.rawQuery = Utils.insertSqlArgs(sql, sqlArgs);
		return query;
	}

    /**
     * Start a query for a single instance of type T
     *
     * @param clazz
     *      The class representing the type of the model you want returned
     *
     * @param sqlResId
     *      The raw sql resource id that should be executed.
     *
     * @param sqlArgs
     *      The array of arguments to insert instead of ? in the placeholderQuery statement.
     *      Strings are automatically placeholderQuery escaped.
     *
     * @param <T>
     *      The type of the model you want returned
     *
     * @return the query to execute
     */
    public static <T extends QueryResult> OneQuery<T> one(Class<T> clazz, int sqlResId,
                                                          Object... sqlArgs) {
        String sql = Utils.readRawText(sqlResId);
        return one(clazz, sql, sqlArgs);
    }

    /**
     * Start a query for a list of instance of type T
     *
     * @param clazz
     *      The class representing the type of the list you want returned
     *
     * @param sql
     *      The raw sql statement that should be executed.
     *
     * @param sqlArgs
     *      The array of arguments to insert instead of ? in the placeholderQuery statement.
     *      Strings are automatically placeholderQuery escaped.
     *
     * @param <T>
     *      The type of the list you want returned
     *
     * @return the query to execute
     */
	public static <T extends QueryResult> ManyQuery<T> many(Class<T> clazz, String sql,
			Object... sqlArgs) {
		final ManyQuery<T> query = new ManyQuery<T>();
		query.resultClass = clazz;
        query.placeholderQuery = sql;
		query.rawQuery = Utils.insertSqlArgs(sql, sqlArgs);
		return query;
	}

    /**
     * Start a query for a list of instance of type T
     *
     * @param clazz
     *      The class representing the type of the list you want returned
     *
     * @param sqlResId
     *      The raw sql resource id that should be executed.
     *
     * @param sqlArgs
     *      The array of arguments to insert instead of ? in the placeholderQuery statement.
     *      Strings are automatically placeholderQuery escaped.
     *
     * @param <T>
     *      The type of the list you want returned
     *
     * @return the query to execute
     */
    public static <T extends QueryResult> ManyQuery<T> many(Class<T> clazz, int sqlResId,
                                                            Object... sqlArgs) {
        String sql = Utils.readRawText(sqlResId);
        return many(clazz, sql, sqlArgs);
    }

    /**
     * Start a query for the entire list of instance of type T
     *
     * @param clazz
     *      The class representing the type of the list you want returned
     *
     * @param <T>
     *      The type of the list you want returned
     *
     * @return the query to execute
     */
    public static <T extends Model> ManyQuery<T> all(Class<T> clazz) {
        return many(clazz, "SELECT * FROM " + Utils.getTableName(clazz));
    }

    public static <C extends Model> Query<C> Where(Class<C> modelClazz){
        return new Query<C>(modelClazz);
    }


    Class<T> mClazz;
    LinkedList<String> mSqlStatementList = new LinkedList<String>();
    int mSkip = 0;
    private Query(Class<T> clazz) {
        mClazz = clazz;
        mSqlStatementList.add("SELECT * FROM "+DataResolver.getTableName(clazz) + " WHERE ");
    }

    /***************QueryPart1 start**********************/
    @Override
    public IQueryPart4<T> equalTo(String field,Object value){
        mSqlStatementList.add(field + "='" + value+"'");
        return this;
    }
    @Override
    public IQueryPart4<T> notEqualTo(String field,Object value){
        mSqlStatementList.add(field+"<>'"+value+"'");
        return this;
    }
    @Override
    public IQueryPart1<T> or(){
        mSqlStatementList.add(" OR ");
        return this;
    }
    @Override
    public IQueryPart1<T> and(){
        mSqlStatementList.add(" AND ");
        return this;
    }
    @Override
    public IQueryPart4<T> like(String field,String likeStr){
        mSqlStatementList.add(field + " LIKE '" + likeStr +"'");
        return this;
    }
    @Override
    public IQueryPart4<T> between(String field,Object from,Object to){
        mSqlStatementList.add(field + " BETWEEN '" + from + "' AND '" + to +"'");
        return this;
    }
    @Override
    public IQueryPart4<T> greaterThan(String field,Object value){
        mSqlStatementList.add(field + ">'" + value+"'");
        return this;
    }
    @Override
    public IQueryPart4<T> greaterThanOrEqualTo(String field,int value){
        mSqlStatementList.add(field + ">='" + value+"'");
        return this;
    }
    @Override
    public IQueryPart4<T> lessThan(String field,Object value){
        mSqlStatementList.add(field + "<'" + value+"'");
        return this;
    }
    @Override
    public IQueryPart4<T> lessThanOrEqualTo(String field,Object value){
        mSqlStatementList.add(field + "<='" + value+"'");
        return this;
    }

    /***************QueryPart1 end**********************/
    /***************QueryPart2 start**********************/

    @Override
    public IQueryPart2<T> take(int limit){
        mSqlStatementList.add(" LIMIT " + limit);
        return this;
    }
    @Override
    public IQueryPart2<T> skip(int skip){
        mSkip = skip;
        return this;
    }
    /***************QueryPart2 end**********************/

    /***************common implements***************/
    @Override
    public IQueryPart3<T> orderBy(String field,SortOrder order){
        mSqlStatementList.add(" ORDER BY "+field + " " + order.name());
        return this;
    }

    @Override
    public String build(){
        StringBuilder sqlBuilder = new StringBuilder();
        for(String sqlPart:mSqlStatementList){
            sqlBuilder.append(sqlPart);
        }
        return sqlBuilder.toString();
    }
    @Override
    public ModelList<T> find(){
        String sql = build();
        if(validateQuery()) {
            return ModelList.from(Query.many(this.mClazz, sql).get(),mSkip);
        }else {
            throw new IllegalStateException("illegal sql syntax:"+sql);
        }
    }
    @Override
    public T findSingle(){
        String sql = build();
        if(!sql.contains("LIMIT")){
            take(1);
            sql = build();
        }
        if(validateQuery()) {
            return Query.one(this.mClazz, sql).get();
        }else {
            throw new IllegalStateException("illegal sql syntax:"+sql);
        }
    }

    boolean validateQuery(){
        //validate  syntax of the sql string
//        new BigDecimal().toBigInteger()
        return true;
    }

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
