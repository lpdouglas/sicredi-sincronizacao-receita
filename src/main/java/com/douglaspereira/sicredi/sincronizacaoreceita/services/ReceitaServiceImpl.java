package com.douglaspereira.sicredi.sincronizacaoreceita.services;

import org.springframework.stereotype.Service;

@Service
public class ReceitaServiceImpl implements ReceitaService{

    private final com.douglaspereira.sicredi.sincronizacaoreceita.external_api.ReceitaService receitaService;

    public ReceitaServiceImpl(com.douglaspereira.sicredi.sincronizacaoreceita.external_api.ReceitaService receitaService) {
        this.receitaService = receitaService;
    }

    @Override
    public boolean atualizarConta(String agencia, String conta, double saldo, String status) {
        return false;
    }
}
