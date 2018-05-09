package com.lsjwzh.orm;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart4<T extends Model> extends IQueryPart2<T> {
    IQueryPart1<T> or();
    IQueryPart1<T> and();
}
