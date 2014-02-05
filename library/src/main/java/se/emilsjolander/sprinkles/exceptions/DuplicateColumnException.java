package se.emilsjolander.sprinkles.exceptions;

public class DuplicateColumnException extends RuntimeException {

	public DuplicateColumnException(String columnName) {
		super(String.format("Column %s is declared multiple times", columnName));
	}

}
