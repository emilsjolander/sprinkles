package se.emilsjolander.sprinkles.exceptions;

public class CannotCascadeDeleteNonForeignKey extends RuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4626270583942735558L;
	
	public CannotCascadeDeleteNonForeignKey() {
		super("A @CascadeDelete annotation may only be present on a field with a @ForeignKey annotation");
	}

}
