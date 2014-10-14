package se.emilsjolander.sprinkles;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart2<T extends Model> extends IQueryPart3<T> {
    public IQueryPart2<T> take(int limit);
    public IQueryPart2<T> skip(int skip);
    public  IQueryPart3<T> orderBy(String field,Query.SortOrder order);

}
