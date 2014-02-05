package se.emilsjolander.sprinkles.exceptions;

public class MultipleAutoIncrementFieldsException extends RuntimeException {

	public MultipleAutoIncrementFieldsException() {
		super("No table is allowed to declare more than one @AutoIncrement field");
	}
	
}
