package se.emilsjolander.sprinkles.exceptions;

/**
 * Created by panwenye on 14-10-15.
 */
public class LazyModelLoadFailException extends RuntimeException {

    public LazyModelLoadFailException(Exception e) {
        super(e);
    }

}

