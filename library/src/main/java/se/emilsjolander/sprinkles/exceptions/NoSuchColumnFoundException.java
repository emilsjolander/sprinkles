package se.emilsjolander.sprinkles.exceptions;

public class NoSuchColumnFoundException extends RuntimeException {

	public NoSuchColumnFoundException(String column) {
		super(String.format("Column %s does not exist", column));
	}

}
