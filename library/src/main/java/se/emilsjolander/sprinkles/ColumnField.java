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
		if (this == o) {
			return true;
		}
		if (!(o instanceof ColumnField)) {
			return false;
		}

		ColumnField that = (ColumnField) o;

		return name != null ? name.equals(that.name) : that.name == null;
	}

	@Override
	public int hashCode() {
		return name != null ? name.hashCode() : 0;
	}
}
