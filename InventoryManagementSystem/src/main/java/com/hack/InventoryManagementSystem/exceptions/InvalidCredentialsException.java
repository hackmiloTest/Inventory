package com.hack.InventoryManagementSystem.exceptions;

public class InvalidCredentialsException extends RuntimeException{

    public InvalidCredentialsException(String message){
        super((message));
    }
}
