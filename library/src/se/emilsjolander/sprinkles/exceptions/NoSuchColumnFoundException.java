package se.emilsjolander.sprinkles.exceptions;

public class NoSuchColumnFoundException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2204104607828295235L;
	
	public NoSuchColumnFoundException(String column) {
		super(String.format("Column %s does not exist", column));
	}

}
