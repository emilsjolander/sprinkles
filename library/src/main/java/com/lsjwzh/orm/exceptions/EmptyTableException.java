package com.lsjwzh.orm.exceptions;

public class EmptyTableException extends RuntimeException {

	public EmptyTableException(String clazz) {
		super(String.format("Class %s does not declare any fields", clazz));
	}

}
