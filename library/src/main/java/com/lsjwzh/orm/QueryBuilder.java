package com.lsjwzh.orm;

import android.text.TextUtils;

import java.util.LinkedList;

/**
 * A simple sql builder.
 */
public class QueryBuilder<T extends Model> implements IQueryWhere<T>, IQueryPart1<T>, IQueryPart2<T>, IQueryPart3<T>, IQueryPart4<T> {

    public enum SortOrder {
        ASC, DESC
    }

    public static <T extends Model> IQueryWhere<T> from(Class<T> clazz) {
        return new QueryBuilder<>(clazz);
    }

    Class<T> modelClass;
    private LinkedList<String> sqlStatementList = new LinkedList<>();
    private LinkedList<Object> sqlArgList = new LinkedList<>();
    private String initSql;
    int skip = 0;

    private QueryBuilder(Class<T> clazz) {
        modelClass = clazz;
        initSql = "SELECT * FROM " + DataResolver.getTableName(clazz);
    }

    /**
     * just for syntax
     */
    @Override
    public QueryBuilder<T> where() {
        return this;
    }

    /***************
     * QueryPart1 start
     **********************/
    @Override
    public IQueryPart4<T> equalTo(String field, Object value) {
        sqlStatementList.add(field + "=?");
        sqlArgList.add(value);
        return this;
    }

    @Override
    public IQueryPart4<T> notEqualTo(String field, Object value) {
        sqlStatementList.add(field + "<>?");
        sqlArgList.add(value);
        return this;
    }

    @Override
    public IQueryPart1<T> or() {
        sqlStatementList.add(" OR ");
        return this;
    }

    @Override
    public IQueryPart1<T> and() {
        sqlStatementList.add(" AND ");
        return this;
    }

    @Override
    public IQueryPart4<T> like(String field, String likeStr) {
        sqlStatementList.add(field + " LIKE '" + likeStr + "'");
        return this;
    }

    @Override
    public IQueryPart4<T> between(String field, Object from, Object to) {
        sqlStatementList.add(field + " BETWEEN ? AND ?");
        sqlArgList.add(from);
        sqlArgList.add(to);
        return this;
    }

    @Override
    public IQueryPart4<T> greaterThan(String field, Object value) {
        sqlStatementList.add(field + ">?");
        sqlArgList.add(value);
        return this;
    }

    @Override
    public IQueryPart4<T> greaterThanOrEqualTo(String field, int value) {
        sqlStatementList.add(field + ">=?");
        sqlArgList.add(value);
        return this;
    }

    @Override
    public IQueryPart4<T> lessThan(String field, Object value) {
        sqlStatementList.add(field + "<?");
        sqlArgList.add(value);
        return this;
    }

    @Override
    public IQueryPart4<T> lessThanOrEqualTo(String field, Object value) {
        sqlStatementList.add(field + "<=?");
        sqlArgList.add(value);
        return this;
    }

    /***************QueryPart1 end**********************/
    /***************
     * QueryPart2 start
     **********************/

    @Override
    public IQueryPart2<T> take(int limit) {
        sqlStatementList.add(" LIMIT " + limit);
        return this;
    }

    @Override
    public IQueryPart2<T> skip(int skip) {
        this.skip = skip;
        return this;
    }
    /***************QueryPart2 end**********************/

    /***************
     * common implements
     ***************/
    @Override
    public IQueryPart3<T> orderBy(String field, SortOrder order) {
        sqlStatementList.add(" ORDER BY " + field + " " + order.name());
        return this;
    }

    @Override
    public QueryBuilder<T> end() {
        if (!TextUtils.isEmpty(sqlStatementList.getLast())) {
            String lastStatement = sqlStatementList.getLast().toLowerCase().trim();
            if (lastStatement.endsWith("or") || lastStatement.endsWith("or")) {
                sqlStatementList.removeLast();
            }
        }
        return this;
    }

    @Override
    public String build(Sprinkles sprinkles) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append(initSql);
        if (sqlStatementList.size() > 0) {
            sqlBuilder.append(" WHERE ");
        }
        for (String sqlPart : sqlStatementList) {
            sqlBuilder.append(sqlPart);
        }
        return Utils.insertSqlArgs(sprinkles, sqlBuilder.toString(), sqlArgList.toArray());
    }

}
