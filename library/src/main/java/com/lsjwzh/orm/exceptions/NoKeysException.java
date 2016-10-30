package com.lsjwzh.orm.exceptions;

public class NoKeysException extends RuntimeException {

	public NoKeysException() {
		super("Every model must have at least one key!");
	}

}
