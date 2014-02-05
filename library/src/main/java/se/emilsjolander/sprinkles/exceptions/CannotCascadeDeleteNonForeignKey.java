package se.emilsjolander.sprinkles.exceptions;

public class CannotCascadeDeleteNonForeignKey extends RuntimeException {

	public CannotCascadeDeleteNonForeignKey() {
		super("A @CascadeDelete annotation may only be present on a field with a @ForeignKey annotation");
	}

}
