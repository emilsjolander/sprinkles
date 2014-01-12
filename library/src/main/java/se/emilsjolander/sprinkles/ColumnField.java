package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;

import se.emilsjolander.sprinkles.annotations.ConflictClause;

class ColumnField {

	String name;
	String type;
	String foreignKey;
	ConflictClause uniqueConflictClause;
	
	boolean isPrimaryKey;
	boolean isForeignKey;
	boolean isAutoIncrementPrimaryKey;
	boolean isCascadeDelete;
	boolean isUnique;

	public Field field;

	@Override
	public boolean equals(Object o) {
		if (o instanceof ColumnField) {
			return ((ColumnField) o).name.equals(name);
		}
		return false;
	}

}
