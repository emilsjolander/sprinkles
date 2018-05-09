package com.lsjwzh.orm.exceptions;

public class IllegalOneToManyColumnException extends RuntimeException {

	public IllegalOneToManyColumnException() {
		super("Type of OneToManyColumnField must be subclass of ModelList");
	}

}
