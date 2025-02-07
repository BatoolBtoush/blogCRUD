package com.batool.crud.exception;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.validation.ValidationException;

@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler({MethodArgumentNotValidException.class, ValidationException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public String handleValidationException(Exception exception) {

        final String[] errors = {""};

        if (exception instanceof MethodArgumentNotValidException){
            MethodArgumentNotValidException methodArgumentNotValidException = (MethodArgumentNotValidException) exception;
            methodArgumentNotValidException.getBindingResult().getAllErrors().forEach((error) -> {
                if (error instanceof FieldError) {
                    String errorMessage = error.getDefaultMessage();
                    errors[0] = new JSONObject().put("message",errorMessage).toString();
                } else if (error instanceof ObjectError) {
                    String errorMessage = error.getDefaultMessage();
                    errors[0]  = new JSONObject().put("message",errorMessage).toString();
                }
            });
        }
        else if (exception instanceof ValidationException) {
            ValidationException validationEx = (ValidationException) exception;
            errors[0] = new JSONObject().put("message", validationEx.getMessage()).toString();
        }

        return errors[0];
    }


}


