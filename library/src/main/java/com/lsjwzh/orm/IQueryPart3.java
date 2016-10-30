package com.lsjwzh.orm;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart3<T extends Model> {
    String build(Sprinkles sprinkles);
    QueryBuilder<T> end();
}
