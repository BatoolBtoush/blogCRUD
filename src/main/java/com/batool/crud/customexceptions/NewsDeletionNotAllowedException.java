package com.batool.crud.customexceptions;

public class NewsDeletionNotAllowedException extends RuntimeException {
    public NewsDeletionNotAllowedException(String message) {
        super(message);
    }
}