package com.zinkworks.atm.exceptions;

public class IncorrectAccountInformationException extends RuntimeException {

    public IncorrectAccountInformationException() {
        super("Incorrect account information");
    }
}
