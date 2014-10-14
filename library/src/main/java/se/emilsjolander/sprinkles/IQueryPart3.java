package se.emilsjolander.sprinkles;

/**
 * IQueryPart1-4 implement series of constraint to avoid sql syntax error
 */
public interface IQueryPart3<T extends Model> {
    public String build();
    public ModelList<T> find();
    public T findSingle();
}
