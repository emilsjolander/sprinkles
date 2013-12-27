package se.emilsjolander.sprinkles.exceptions;

public class AutoIncrementMustBeIntegerException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3214776964727149312L;

	public AutoIncrementMustBeIntegerException(String column) {
		super(String.format("The column %s was marked as a @AutoIncrement field but is not a int or a long", column));
	}
	
}
