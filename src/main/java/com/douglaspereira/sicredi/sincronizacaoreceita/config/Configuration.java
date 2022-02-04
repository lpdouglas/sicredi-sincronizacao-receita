package com.douglaspereira.sicredi.sincronizacaoreceita.config;

import com.douglaspereira.sicredi.sincronizacaoreceita.external.api.ReceitaService;
import org.springframework.context.annotation.Bean;

@org.springframework.context.annotation.Configuration
public class Configuration {
    @Bean
    public ReceitaService receitaService() {
        return new ReceitaService();
    }
}