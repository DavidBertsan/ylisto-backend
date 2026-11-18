package com.ylisto.clocking;

public class AlreadyClockedInException extends RuntimeException {
    public AlreadyClockedInException(String message) {
        super(message);
    }
}
