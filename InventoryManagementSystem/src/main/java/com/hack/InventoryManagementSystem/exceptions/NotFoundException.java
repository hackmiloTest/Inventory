package com.hack.InventoryManagementSystem.exceptions;

public class NotFoundException extends RuntimeException{

    public NotFoundException(String message){
        super((message));
    }
}
