package com.uniquindio.fintech.exception;

public class OperacionInvalidaException extends RuntimeException {
    public OperacionInvalidaException(String mensaje) {
        super(mensaje);
    }
}