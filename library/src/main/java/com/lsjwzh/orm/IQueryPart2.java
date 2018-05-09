package com.lsjwzh.orm;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart2<T extends Model> extends IQueryPart3<T> {
    IQueryPart2<T> take(int limit);
    IQueryPart2<T> skip(int skip);
    IQueryPart3<T> orderBy(String field,QueryBuilder.SortOrder order);

}
