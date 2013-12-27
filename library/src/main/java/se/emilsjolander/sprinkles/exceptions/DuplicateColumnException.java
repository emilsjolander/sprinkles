package se.emilsjolander.sprinkles.exceptions;

public class DuplicateColumnException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3093784258060286873L;
	
	public DuplicateColumnException(String columnName) {
		super(String.format("Column %s is declared mutiple times", columnName));
	}

}
