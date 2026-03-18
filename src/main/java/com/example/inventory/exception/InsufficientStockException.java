package com.example.inventory.exception;
public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super("Insufficient stock for product " + productId + ". Requested: " + requested + ", Available: " + available);
    }
}
