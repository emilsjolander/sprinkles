package se.emilsjolander.sprinkles.exceptions;

public class NoTableAnnotationException extends RuntimeException {

	public NoTableAnnotationException() {
		super("Your model must be annotated with an @Table annotation");
	}

}
