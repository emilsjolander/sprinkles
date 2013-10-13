package se.emilsjolander.sprinkles;

import java.lang.reflect.Field;

class ColumnField {

	String name;
	String type;
	String foreignKey;
	
	boolean isPrimaryKey;
	boolean isForeignKey;
	boolean isAutoIncrementPrimaryKey;
	boolean isCascadeDelete;
	
	public Field field;
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof ColumnField) {
			return ((ColumnField) o).name.equals(name);
		}
		return false;
	}

}
