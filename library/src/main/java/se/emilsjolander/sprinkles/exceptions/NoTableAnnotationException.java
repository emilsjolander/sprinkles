package se.emilsjolander.sprinkles.exceptions;

public class NoTableAnnotationException extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7192150829452806874L;
	
	public NoTableAnnotationException() {
		super("Your model must be annotated with an @Table annotation");
	}

}
