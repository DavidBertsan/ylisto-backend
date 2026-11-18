package com.ylisto.clocking;

public class NotClockedInException extends RuntimeException {
    public NotClockedInException(String message) {
        super(message);
    }
}
