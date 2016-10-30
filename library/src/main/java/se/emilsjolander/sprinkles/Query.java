package se.emilsjolander.sprinkles;

import java.util.LinkedList;

import android.support.annotation.NonNull;

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
    public static <T extends QueryResult> OneQuery<T> one(@NonNull Sprinkles sprinkles,
                                                          @NonNull Class<T> clazz, int sqlResId,
                                                          Object... sqlArgs) {
        String sql = Utils.readRawText(sprinkles.mContext.getResources(), sqlResId);
        return one(sprinkles, clazz, sql, sqlArgs);
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
    public static <T extends QueryResult> ManyQuery<T> many(@NonNull Sprinkles sprinkles,
                                                            @NonNull Class<T> clazz, int sqlResId,
                                                            Object... sqlArgs) {
        String sql = Utils.readRawText(sprinkles.mContext.getResources(), sqlResId);
        return many(sprinkles, clazz, sql, sqlArgs);
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
    public static <T extends Model> ManyQuery<T> all(@NonNull Sprinkles sprinkles, @NonNull Class<T> clazz) {
        return many(sprinkles, clazz, "SELECT * FROM " + DataResolver.getTableName(clazz));
    }

    public static <C extends Model> Query<C> where(@NonNull Sprinkles sprinkles, Class<C> modelClazz){
        return new Query<>(sprinkles, modelClazz);
    }



    Sprinkles sprinkles;
    Class<T> mClazz;
    LinkedList<String> mSqlStatementList = new LinkedList<String>();
    LinkedList<Object> mSqlArgList = new LinkedList<Object>();
    String mInitSql;
    int mSkip = 0;
    private Query(Sprinkles sprinkles, Class<T> clazz) {
        this.sprinkles = sprinkles;
        mClazz = clazz;
        mInitSql = "SELECT * FROM "+DataResolver.getTableName(clazz);
    }

    /***************QueryPart1 start**********************/
    @Override
    public IQueryPart4<T> equalTo(String field,Object value){
        mSqlStatementList.add(field + "=?");
        mSqlArgList.add(value);
        return this;
    }
    @Override
    public IQueryPart4<T> notEqualTo(String field,Object value){
        mSqlStatementList.add(field+"<>?");
        mSqlArgList.add(value);
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
        mSqlStatementList.add(field + " BETWEEN ? AND ?");
        mSqlArgList.add(from);
        mSqlArgList.add(to);
        return this;
    }
    @Override
    public IQueryPart4<T> greaterThan(String field,Object value){
        mSqlStatementList.add(field + ">?");
        mSqlArgList.add(value);
        return this;
    }
    @Override
    public IQueryPart4<T> greaterThanOrEqualTo(String field,int value){
        mSqlStatementList.add(field + ">=?");
        mSqlArgList.add(value);
        return this;
    }
    @Override
    public IQueryPart4<T> lessThan(String field,Object value){
        mSqlStatementList.add(field + "<?");
        mSqlArgList.add(value);
        return this;
    }
    @Override
    public IQueryPart4<T> lessThanOrEqualTo(String field,Object value){
        mSqlStatementList.add(field + "<=?");
        mSqlArgList.add(value);
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
        sqlBuilder.append(mInitSql);
        if(mSqlStatementList.size()>0){
            sqlBuilder.append(" WHERE ");
        }
        for(String sqlPart:mSqlStatementList){
            sqlBuilder.append(sqlPart);
        }
        return Utils.insertSqlArgs(sprinkles, sqlBuilder.toString(), mSqlArgList.toArray());
    }
    @Override
    public ModelList<T> find(){
        String sql = build();
        CursorList<T> cursorList = Query.many(sprinkles, this.mClazz, sql).get();
        ModelList<T> result = ModelList.from(cursorList,mSkip);
        cursorList.close();
        return result;
    }
    @Override
    public T findSingle(){
        String sql = build();
        if(!sql.contains("LIMIT")){
            take(1);
            sql = build();
        }
        return Query.one(sprinkles, this.mClazz, sql).get();
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
