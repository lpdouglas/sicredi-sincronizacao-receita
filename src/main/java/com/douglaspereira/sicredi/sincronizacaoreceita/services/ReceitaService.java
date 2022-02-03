package com.douglaspereira.sicredi.sincronizacaoreceita.services;

public interface ReceitaService {
    public boolean atualizarConta(String agencia, String conta, double saldo, String status);
}
