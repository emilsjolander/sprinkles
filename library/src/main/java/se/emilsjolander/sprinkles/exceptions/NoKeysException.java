package se.emilsjolander.sprinkles.exceptions;

public class NoKeysException extends RuntimeException {

	public NoKeysException() {
		super("Every model must have at least one key!");
	}

}
