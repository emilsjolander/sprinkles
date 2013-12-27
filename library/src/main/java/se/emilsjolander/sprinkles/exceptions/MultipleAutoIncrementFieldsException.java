package se.emilsjolander.sprinkles.exceptions;

public class MultipleAutoIncrementFieldsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 432376145867788649L;

	public MultipleAutoIncrementFieldsException() {
		super("No table is allowed to declare more than one @AutoIncrement field");
	}
	
}
