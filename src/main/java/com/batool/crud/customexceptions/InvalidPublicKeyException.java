package com.batool.crud.customexceptions;

public class InvalidPublicKeyException extends RuntimeException {
    public InvalidPublicKeyException(String message, Throwable cause) {
        super(message, cause);
    }
}
