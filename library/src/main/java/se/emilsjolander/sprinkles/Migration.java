package se.emilsjolander.sprinkles;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

import se.emilsjolander.sprinkles.annotations.ConflictClause;
import se.emilsjolander.sprinkles.exceptions.NoSuchColumnFoundException;

/**
 * A class representing database migration. Multiple statements can be made within one migration.
 * Once a production app has shipped with a migration it should never be altered or removed.
 * Sprinkles will make sure that the correct migrations are performed when a user upgrades to the
 * latest version of your app.
 */
public class Migration {

	List<String> mStatements = new ArrayList<String>();

	void execute(SQLiteDatabase db) {
		for (String sql : mStatements) {
			db.execSQL(sql);
		}
	}

	/**
	 * Create a table
	 *
	 * @param clazz The class representing the table to add. This will also add all the annotated
	 *              columns within that class.
	 * @return this Migration instance
	 */
	public Migration createTable(Class<? extends Model> clazz) {
        final ModelInfo info = ModelInfo.from(clazz);
		final StringBuilder createStatement = new StringBuilder();

		createStatement.append("CREATE TABLE ");
		createStatement.append(info.tableName);
		createStatement.append("(");

        // only list primary keys in the end if they exists and there is not only one that is autoincrement.
        final boolean appendPrimaryKeys =
                !(info.primaryKeys.isEmpty() || info.primaryKeys.size() == 1 && info.autoIncrementColumn != null);
        final boolean appendForeignKeys = !info.foreignKeys.isEmpty();

		for (int i = 0; i < info.staticColumns.size(); i++) {
			final ModelInfo.StaticColumnField column = info.staticColumns.get(i);
			createStatement.append(column.name + " ");
			createStatement.append(column.sqlType);

			if (column.isAutoIncrement && column.isPrimaryKey) {
				createStatement.append(" PRIMARY KEY AUTOINCREMENT");
			}
			if (column.isUnique) {
				createStatement.append(" UNIQUE");
				if (column.uniqueConflictClause != ConflictClause.DEFAULT) {
					createStatement.append(" ON CONFLICT ");
					createStatement.append(column.uniqueConflictClause.toString());
				}
			}
            if (column.isNotNull) {
                createStatement.append(" NOT NULL");
            }
            if (column.hasCheck) {
                createStatement.append(" CHECK("+column.checkClause+")");
            }

			// add a comma separator between columns if it is not the last column
			if (i < info.staticColumns.size() - 1 || appendPrimaryKeys || appendForeignKeys) {
				createStatement.append(", ");
			}
		}

		if (appendPrimaryKeys) {
			createStatement.append("PRIMARY KEY(");

			for (int i = 0; i < info.primaryKeys.size(); i++) {
				final ModelInfo.StaticColumnField column = info.primaryKeys.get(i);

                // autoincrement primary key constraint cannot be listed in the back of the query.
                if (info.autoIncrementColumn == column) {
                    continue;
                }

				createStatement.append(column.name);

				// add a comma separator between keys if it is not the last primary key
				if (i < info.primaryKeys.size() - 1) {
					createStatement.append(", ");
				}
			}

			createStatement.append(")");

			// add a comma separator if there are foreign keys to add
			if (appendForeignKeys) {
				createStatement.append(", ");
			}
		}

		for (int i = 0; i < info.foreignKeys.size(); i++) {
            final ModelInfo.StaticColumnField column = info.foreignKeys.get(i);
			createStatement.append("FOREIGN KEY(");
			createStatement.append(column.name);
			createStatement.append(") REFERENCES ");
			createStatement.append(column.foreignKey);
			if (column.isCascadeDelete) {
				createStatement.append(" ON DELETE CASCADE");
			}

			// add a comma separator if there are still foreign keys to add
			if (i < info.foreignKeys.size() - 1) {
				createStatement.append(", ");
			}
		}

		createStatement.append(");");

		mStatements.add(createStatement.toString());
		return this;
	}

	/**
	 * Remove a table
	 *
	 * @param clazz The class representing the table to remove
	 * @return this Migration instance
	 */
	public Migration dropTable(Class<? extends Model> clazz) {
		final String tableName = Utils.getTableName(clazz);
		mStatements.add(String.format("DROP TABLE IF EXISTS %s;", tableName));
		return this;
	}

	/**
	 * Rename a table
	 *
	 * @param from The current tableName
	 * @param to   The new tableName
	 * @return this Migration instance
	 */
	public Migration renameTable(String from, String to) {
		mStatements.add(String.format("ALTER TABLE %s RENAME TO %s;", from, to));
		return this;
	}

	/**
	 * Add a column
	 *
	 * @param clazz      The class representing the table which should hold the new model.
	 * @param columnName The tableName of the new column. The type of the new column is taken from the
	 *                   class.
	 * @return this Migration instance
	 */
	public Migration addColumn(Class<? extends Model> clazz, String columnName) {
        final ModelInfo info = ModelInfo.from(clazz);
		ModelInfo.StaticColumnField newColumn = null;

		for (ModelInfo.StaticColumnField column : info.staticColumns) {
			if (column.name.equals(columnName)) {
                newColumn = column;
			}
		}

		if (newColumn == null) {
			throw new NoSuchColumnFoundException(columnName);
		}

		mStatements.add(String.format("ALTER TABLE %s ADD COLUMN %s %s;",
                info.tableName, newColumn.name, newColumn.sqlType));
		return this;
	}

    public Migration createIndex(String indexName, boolean unique, Class<? extends Model> clazz, String... columnNames) {
        StringBuilder statement = new StringBuilder();
        statement.append("CREATE ");
        if (unique) {
            statement.append("UNIQUE ");
        }
        statement.append("INDEX ");
        statement.append(indexName);
        statement.append(" ");
        statement.append("ON ");
        statement.append(Utils.getTableName(clazz));
        statement.append("(");
        for (String col : columnNames) {
            statement.append(col);
            if (!col.equals(columnNames[columnNames.length-1])) {
                statement.append(",");
            }
        }
        statement.append(");");
        mStatements.add(statement.toString());
        return this;
    }

    public Migration dropIndex(String indexName) {
        mStatements.add(String.format("DROP INDEX IF EXISTS %s;", indexName));
        return this;
    }

	/**
	 * Add a raw sql statement to be executed within a migration.
	 * This should be used when none of the other methods fit your needs.
	 *
	 * @param statement The statement to execute
	 * @return this Migration instance
	 */
	public Migration addRawStatement(String statement) {
		mStatements.add(statement);
		return this;
	}

}
