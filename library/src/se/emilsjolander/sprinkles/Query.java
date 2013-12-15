package se.emilsjolander.sprinkles;


public final class Query {

	private Query() {
	}

	public static <T extends Model> OneQuery<T> one(Class<T> clazz, String sql,
			Object... sqlArgs) {
		final OneQuery<T> query = new OneQuery<T>();
		query.resultClass = clazz;
		query.sqlQuery = Utils.insertSqlArgs(sql, sqlArgs);
		return query;
	}

	public static <T extends Model> ManyQuery<T> many(Class<T> clazz, String sql,
			Object... sqlArgs) {
		final ManyQuery<T> query = new ManyQuery<T>();
		query.resultClass = clazz;
		query.sqlQuery = Utils.insertSqlArgs(sql, sqlArgs);
		return query;
	}

}
