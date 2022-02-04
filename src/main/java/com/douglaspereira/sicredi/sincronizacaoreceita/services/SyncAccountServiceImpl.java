package com.douglaspereira.sicredi.sincronizacaoreceita.services;

import com.douglaspereira.sicredi.sincronizacaoreceita.enums.StatusContaEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.enums.SyncResultEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.exceptions.BusinessException;
import com.douglaspereira.sicredi.sincronizacaoreceita.external.api.ReceitaService;
import com.douglaspereira.sicredi.sincronizacaoreceita.pojos.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

@Service
public class SyncAccountServiceImpl implements SyncAccountService {
    private static final String ERROR_MAPING_ACCOUNTS_FROM_FILE = "Error maping accounts from file";
    private static final String ERROR_SAVING_FILE = "Error saving file";
    private final Logger log;
    private static final String FIELD_ACCOUNT = "conta";
    private static final String FIELD_AGENCY = "agencia";
    private static final String FIELD_BALANCE = "saldo";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SYNC_RESULT = "sincronizado";
    private static final String CSV_FIELDS_FORMAT = "%s;%s;%s;%s;%s";
    private static final String CSV_FIELDS_FORMAT_SAVE = "%n%s;%s;%s;%s;%s";
    private static final String CSV_FIELD_SEPARATOR = ";";
    private final ReceitaService receitaService;

    public SyncAccountServiceImpl(ReceitaService receitaService) {
        this.receitaService = receitaService;
        this.log = LoggerFactory.getLogger(SyncAccountServiceImpl.class);
    }

    @Override
    public void syncAccountsFromFile(File accountsFile, File targetFile) {
        log.info("Start Process: Reading from file {}", accountsFile.getAbsoluteFile());

        try (InputStream fileInputStream = new FileInputStream(accountsFile);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));
             FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream))) {

            DecimalFormat brazilCurrency = (DecimalFormat) NumberFormat.getNumberInstance(new Locale("pt", "br"));
            brazilCurrency.applyPattern("#0.00");
            bufferedWriter.write(String.format(CSV_FIELDS_FORMAT, FIELD_AGENCY, FIELD_ACCOUNT, FIELD_BALANCE, FIELD_STATUS, FIELD_SYNC_RESULT));
            bufferedReader.lines()
                    .skip(1)
                    .parallel()
                    .forEach(line -> {
                        Account account = getAccountFromString(line);
                        syncAccount(account);
                        saveAccountOnCsvFile(account, bufferedWriter, brazilCurrency);
                        log.info("Processed Account {} with Banco Central", account.getAccount());
                    });

        } catch (IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
            throw new BusinessException(ERROR_MAPING_ACCOUNTS_FROM_FILE, e);
        }

        log.info("Ended Process: Saved in the file {}", targetFile.getAbsoluteFile());
    }

    private Account getAccountFromString(String line) {
        String[] p = line.split(CSV_FIELD_SEPARATOR);
        return new Account(p[0], p[1], Double.parseDouble(p[2].replace(",", ".")), StatusContaEnum.valueOf(p[3]));
    }


    private void syncAccount(Account account) {
        try {
            boolean isContaAtualizada = receitaService.atualizarConta(
                    account.getAgency(),
                    account.getAccount().replace("-", ""),
                    account.getBalance(),
                    account.getStatus().name());
            account.setResult(isContaAtualizada ? SyncResultEnum.SYNCRONIZED : SyncResultEnum.NOT_SYNCRONIZED);
        } catch (InterruptedException | RuntimeException e) {
            account.setResult(SyncResultEnum.ERROR);
        }
    }

    private void saveAccountOnCsvFile(Account account, BufferedWriter bufferedWriter, DecimalFormat currencyFormat) throws BusinessException {
        try {
            bufferedWriter.write(String.format(CSV_FIELDS_FORMAT_SAVE,
                    account.getAgency(),
                    account.getAccount(),
                    currencyFormat.format(account.getBalance()),
                    account.getStatus(),
                    account.getResult()));

        } catch (IOException e) {
            throw new BusinessException(ERROR_SAVING_FILE);
        }
    }
}