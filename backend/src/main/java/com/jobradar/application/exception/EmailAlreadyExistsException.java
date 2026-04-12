package com.jobradar.application.exception;

public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException(String email){
        super("User with Email: " +email+" already exists");
    }
}
