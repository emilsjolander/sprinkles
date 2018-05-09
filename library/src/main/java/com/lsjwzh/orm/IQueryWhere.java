package com.lsjwzh.orm;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryWhere<T extends Model> {
    QueryBuilder<T> where();
}
