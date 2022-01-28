package com.douglaspereira.sicredi.sincronizacaoreceita.exceptions;

public class BusinessException extends RuntimeException {
    private String message;

    public BusinessException(String message) {
        super(message);
    }
}
