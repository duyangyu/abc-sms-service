package org.theabconline.smsservice.exception;

public class UserCreationException extends RuntimeException {

    public UserCreationException(String errorMessage) {
        super(errorMessage);
    }
}
