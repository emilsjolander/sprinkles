package se.emilsjolander.sprinkles;

/**
 * Contains static methods to initiate queries
 */
public final class Query {

	private Query() {
	}

    /**
     * Start a query for a single instance of type T
     *
     * @param clazz
     *      The class representing the type of the model you want returned
     *
     * @param sql
     *      The raw sql statement that should be executed.
     *
     * @param sqlArgs
     *      The array of arguments to insert instead of ? in the sql statement.
     *      Strings are automatically sql escaped.
     *
     * @param <T>
     *      The type of the model you want returned
     *
     * @return the query to execute
     */
	public static <T extends QueryResult> OneQuery<T> one(Class<T> clazz, String sql,
			Object... sqlArgs) {
		final OneQuery<T> query = new OneQuery<T>();
		query.resultClass = clazz;
		query.sqlQuery = Utils.insertSqlArgs(sql, sqlArgs);
		return query;
	}

    /**
     * Start a query for a list of instance of type T
     *
     * @param clazz
     *      The class representing the type of the list you want returned
     *
     * @param sql
     *      The raw sql statement that should be executed.
     *
     * @param sqlArgs
     *      The array of arguments to insert instead of ? in the sql statement.
     *      Strings are automatically sql escaped.
     *
     * @param <T>
     *      The type of the list you want returned
     *
     * @return the query to execute
     */
	public static <T extends QueryResult> ManyQuery<T> many(Class<T> clazz, String sql,
			Object... sqlArgs) {
		final ManyQuery<T> query = new ManyQuery<T>();
		query.resultClass = clazz;
		query.sqlQuery = Utils.insertSqlArgs(sql, sqlArgs);
		return query;
	}

    /**
     * Start a query for the entire list of instance of type T
     *
     * @param clazz
     *      The class representing the type of the list you want returned
     *
     * @param <T>
     *      The type of the list you want returned
     *
     * @return the query to execute
     */
    public static <T extends Model> ManyQuery<T> all(Class<T> clazz) {
        return many(clazz, "SELECT * FROM " + Utils.getTableName(clazz));
    }
}
