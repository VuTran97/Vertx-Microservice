package com.example.demo.exception;

public class VerticleException extends RuntimeException{

    private int statusCode;

    public VerticleException() {
        super();
        statusCode = 500;
    }

    public VerticleException(int code) {
        super();
        statusCode = code;
    }

    public VerticleException(String message) {
        super(message);
        statusCode = 500;
    }

    public VerticleException(String message, Throwable cause) {
        super(message, cause);
        statusCode = 500;
    }

    public VerticleException(String message, int code) {
        super(message);
        statusCode = code;
    }

    public VerticleException(Throwable cause, int code) {
        super(cause);
        statusCode = code;
    }

    public VerticleException(String message, Throwable cause, int code) {
        super(message, cause);
        statusCode = code;
    }

    public int getStatusCode() {
        return statusCode;
    }

}
