package se.emilsjolander.sprinkles.exceptions;

public class EmptyTableException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8685887532184477033L;
	
	public EmptyTableException(String clazz) {
		super(String.format("Class %s does not declare any fields", clazz));
	}

}
