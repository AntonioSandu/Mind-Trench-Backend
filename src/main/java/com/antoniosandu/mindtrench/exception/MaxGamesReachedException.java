package com.antoniosandu.mindtrench.exception;

public class MaxGamesReachedException extends RuntimeException {

    public MaxGamesReachedException(String message) {
        super(message);
    }
}
