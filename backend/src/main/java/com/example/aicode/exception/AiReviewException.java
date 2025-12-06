package com.example.aicode.exception;

public class AiReviewException extends RuntimeException {

    public AiReviewException(String message, Throwable cause) {
        super(message, cause);
    }

    public AiReviewException(String message) {
        super(message);
    }
}
