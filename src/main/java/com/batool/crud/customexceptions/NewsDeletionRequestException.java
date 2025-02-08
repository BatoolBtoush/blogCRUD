package com.batool.crud.customexceptions;

public class NewsDeletionRequestException extends RuntimeException {
    public NewsDeletionRequestException(String message) {
        super(message);
    }
}
