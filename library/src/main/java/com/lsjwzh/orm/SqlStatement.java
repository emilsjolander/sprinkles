package com.lsjwzh.orm;

public class SqlStatement {

    final Sprinkles sprinkles;
    String sql;

    public SqlStatement(Sprinkles sprinkles, String sql) {
        this.sql = sql;
        this.sprinkles = sprinkles;
    }

    public void execute(Object... args) {
        sprinkles.getDatabase().execSQL(Utils.insertSqlArgs(sprinkles, sql, args));
    }

}
