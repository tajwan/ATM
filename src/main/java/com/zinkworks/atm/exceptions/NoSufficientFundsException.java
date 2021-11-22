package com.zinkworks.atm.exceptions;

public class NoSufficientFundsException extends RuntimeException {
    public NoSufficientFundsException() {
        super("Cannot withdraw money. No sufficient funds.");
    }
}
