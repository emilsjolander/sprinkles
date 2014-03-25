package se.emilsjolander.sprinkles;

public class SqlStatement {

    String sql;

    public SqlStatement(String sql) {
        this.sql = sql;
    }

    public void execute(Object... args) {
        Sprinkles.getDatabase().execSQL(Utils.insertSqlArgs(sql, args));
    }

}
