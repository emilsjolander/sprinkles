package se.emilsjolander.sprinkles.exceptions;

public class NoPrimaryKeysException extends RuntimeException {

	public NoPrimaryKeysException() {
		super("Every model must have at least one primary key!");
	}

}
