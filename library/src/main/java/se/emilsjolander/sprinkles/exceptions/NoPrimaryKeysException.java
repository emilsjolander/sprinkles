package se.emilsjolander.sprinkles.exceptions;

public class NoPrimaryKeysException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4928042808343728820L;
	
	public NoPrimaryKeysException() {
		super("Every model must have atleast one primary key!");
	}

}
