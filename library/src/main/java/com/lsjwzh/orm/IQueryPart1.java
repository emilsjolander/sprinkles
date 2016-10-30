package com.lsjwzh.orm;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart1<T extends Model> {
    IQueryPart4<T> equalTo(String field, Object value);

    IQueryPart4<T> notEqualTo(String field, Object value);

    IQueryPart4<T> like(String field, String likeStr);

    IQueryPart4<T> between(String field, Object from, Object to);

    IQueryPart4<T> greaterThan(String field, Object value);

    IQueryPart4<T> greaterThanOrEqualTo(String field, int value);

    IQueryPart4<T> lessThan(String field, Object value);

    IQueryPart4<T> lessThanOrEqualTo(String field, Object value);

    IQueryPart3<T> orderBy(String field, QueryBuilder.SortOrder order);

    QueryBuilder<T> end();

    String build(Sprinkles sprinkles);
}
