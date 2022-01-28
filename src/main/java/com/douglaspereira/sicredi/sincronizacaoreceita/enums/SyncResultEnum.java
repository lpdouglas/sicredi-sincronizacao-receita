package com.douglaspereira.sicredi.sincronizacaoreceita.enums;

public enum SyncResultEnum {
    SYNCRONIZED("Sim"), NOT_SYNCRONIZED("Não"), ERROR("Erro");


    private final String label;

    SyncResultEnum(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return this.label;
    }
}
