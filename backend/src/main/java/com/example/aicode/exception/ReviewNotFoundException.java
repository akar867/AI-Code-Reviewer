package com.example.aicode.exception;

public class ReviewNotFoundException extends RuntimeException {

    public ReviewNotFoundException(Long id) {
        super("Review %d not found".formatted(id));
    }
}
