package se.emilsjolander.sprinkles;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart4<T extends Model> extends IQueryPart2<T> {
    public IQueryPart1<T> or();
    public IQueryPart1<T> and();

}
