package com.lsjwzh.orm;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart1 <T extends Model>{
    public IQueryPart4<T> equalTo(String field,Object value);
    public IQueryPart4<T> notEqualTo(String field,Object value);
    public IQueryPart4<T> like(String field,String likeStr);
    public IQueryPart4<T> between(String field,Object from,Object to);
    public IQueryPart4<T> greaterThan(String field,Object value);
    public IQueryPart4<T> greaterThanOrEqualTo(String field,int value);
    public IQueryPart4<T> lessThan(String field,Object value);
    public IQueryPart4<T> lessThanOrEqualTo(String field,Object value);

    public  IQueryPart3<T> orderBy(String field,Query.SortOrder order);



    public String build();
    public ModelList<T> find();
    public T findSingle();
}
