package com.example.inventory.exception;
public class TokenExpiredException extends RuntimeException {
    public TokenExpiredException(String message) { super(message); }
}
