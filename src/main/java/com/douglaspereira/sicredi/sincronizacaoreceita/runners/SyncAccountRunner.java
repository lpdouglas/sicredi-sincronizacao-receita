package com.douglaspereira.sicredi.sincronizacaoreceita.runners;

import com.douglaspereira.sicredi.sincronizacaoreceita.exceptions.BusinessException;
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

    public static final String FILENAME_NOT_PASSED_ON_ARGS = "Filename not passed on args";
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
            String filename = getFilenameFromArgs(args);

            Set<Account> accounts = syncAccountService.getAccountsFromFile(filename);
            //Set<Account> accounts = syncAccountService.getAccountsFromMock();

            Set<Account> accountsResult = syncAccountService.syncAccounts(receitaService, accounts);

            syncAccountService.saveAccountsOnFile(filename, accountsResult);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getFilenameFromArgs(ApplicationArguments args) throws Exception {
        String file;
        if (!args.getNonOptionArgs().isEmpty()) {
            file = args.getNonOptionArgs().get(0);
        } else {
            throw new BusinessException(FILENAME_NOT_PASSED_ON_ARGS);
        }
        return file;
    }

}
