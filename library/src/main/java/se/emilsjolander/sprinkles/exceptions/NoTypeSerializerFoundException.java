package se.emilsjolander.sprinkles.exceptions;

public class NoTypeSerializerFoundException extends RuntimeException {

    public NoTypeSerializerFoundException(Class<?> type) {
        super(String.format("Could not serialize field with class %s, no TypeSerializer found.", type.getName()));
    }

}