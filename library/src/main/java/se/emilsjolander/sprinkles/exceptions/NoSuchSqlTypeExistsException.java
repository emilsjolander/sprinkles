package se.emilsjolander.sprinkles.exceptions;

public class NoSuchSqlTypeExistsException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 732723846870837880L;

	public NoSuchSqlTypeExistsException(String fieldName) {
		super(String.format("Field %s has a type that cannot be converted to an sql type", fieldName));
	}
	
}
