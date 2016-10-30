package com.lsjwzh.orm.exceptions;

/**
 * Created by panwenye on 14-10-15.
 */
public class LazyModelListLoadFailException extends RuntimeException {

    public LazyModelListLoadFailException(Exception e) {
        super(e);
    }

}

