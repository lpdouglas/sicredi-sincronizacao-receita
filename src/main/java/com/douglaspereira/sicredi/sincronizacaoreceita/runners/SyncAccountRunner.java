package com.douglaspereira.sicredi.sincronizacaoreceita.runners;

import com.douglaspereira.sicredi.sincronizacaoreceita.external.ReceitaService;
import com.douglaspereira.sicredi.sincronizacaoreceita.pojos.Account;
import com.douglaspereira.sicredi.sincronizacaoreceita.services.SyncAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SyncAccountRunner implements ApplicationRunner {

    private final SyncAccountService syncAccountService;
    private final Logger log;

    public SyncAccountRunner(SyncAccountService syncAccountService) {
        this.syncAccountService = syncAccountService;
        this.log = LoggerFactory.getLogger(SyncAccountRunner.class);
    }

    @Override
    public void run(ApplicationArguments args) {
        ReceitaService receitaService = new ReceitaService();

        try {
            String file = getFilenameFromArgs(args);

            Set<Account> accounts = syncAccountService.getAccountsFromFile(file);

            Set<Account> accountsResult = syncAccountService.syncAccounts(receitaService, accounts);

            syncAccountService.saveAccountsOnFile(file.concat("_Sincronizado"), accountsResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getFilenameFromArgs(ApplicationArguments args) throws Exception {
        String file;
        if (!args.getNonOptionArgs().isEmpty()) {
            log.info("File: {}", args.getNonOptionArgs().get(0));
            file = args.getNonOptionArgs().get(0);
        } else {
            throw new Exception();
        }
        return file;
    }

}
