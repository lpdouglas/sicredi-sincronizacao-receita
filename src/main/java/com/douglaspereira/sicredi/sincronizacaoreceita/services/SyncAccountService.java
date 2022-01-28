package com.douglaspereira.sicredi.sincronizacaoreceita.services;

import com.douglaspereira.sicredi.sincronizacaoreceita.enums.StatusContaEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.enums.SyncResultEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.exceptions.BusinessException;
import com.douglaspereira.sicredi.sincronizacaoreceita.external.ReceitaService;
import com.douglaspereira.sicredi.sincronizacaoreceita.pojos.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SyncAccountService {

    private static final String ERROR_MAPING_ACCOUNTS_FROM_FILE = "Error maping accounts from file";
    private static final String ERROR_SAVING_FILE = "Error saving file";
    private static final String ERROR_CREATING_NEW_FILE = "Error creating new file";
    private final Logger log;
    private static final String FIELD_ACCOUNT = "conta";
    private static final String FIELD_AGENCY = "agencia";
    private static final String FIELD_BALANCE = "saldo";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SYNC_RESULT = "sincronizado";
    private static final String CSV_FIELDS_FORMAT = "%s;%s;%s;%s;%s";
    private static final String CSV_FIELD_SEPARATOR = ";";

    public SyncAccountService() {
        this.log = LoggerFactory.getLogger(SyncAccountService.class);
    }

    public Set<Account> syncAccounts(ReceitaService receitaService, Set<Account> accounts) {
        log.info("Sync Accounts with Banco Central: Started");
        Set<Account> accountsResult = accounts.parallelStream().map(contaDTO -> {
            try {
                boolean isContaAtualizada = receitaService.atualizarConta(
                        contaDTO.getAgency(),
                        contaDTO.getAccount().replace("-", ""),
                        contaDTO.getBalance(),
                        contaDTO.getStatus().name());
                contaDTO.setResult(isContaAtualizada ? SyncResultEnum.SYNCRONIZED : SyncResultEnum.NOT_SYNCRONIZED);

                return contaDTO;
            } catch (InterruptedException | RuntimeException e) {
                contaDTO.setResult(SyncResultEnum.ERROR);
                return contaDTO;
            }
        }).collect(Collectors.toSet());
        log.info("Sync Accounts with Banco Central: Ended");
        return accountsResult;
    }

    public Set<Account> getAccountsFromFile(String file) throws BusinessException {
        try (InputStream fileInputStream = new FileInputStream(new File(file));
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {

            return bufferedReader.lines()
                    .skip(1)
                    .map(mapToAccount)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            throw new BusinessException(ERROR_MAPING_ACCOUNTS_FROM_FILE);
        }
    }

    public Set<Account> getAccountsFromMock() {
        Set<Account> accounts = new HashSet<>();
        accounts.add(new Account("0101", "12345-6", 100.50, StatusContaEnum.A));
        accounts.add(new Account("0202", "23456-7", 200.40, StatusContaEnum.B));
        accounts.add(new Account("0301", "34567-8", 300.30, StatusContaEnum.I));
        accounts.add(new Account("0301", "34NOT7-8", 300.30, StatusContaEnum.I));
        accounts.add(new Account("0403", "45678-9", 400.20, StatusContaEnum.P));
        accounts.add(new Account("0410", "46789-0", 700.00, StatusContaEnum.B));
        accounts.add(new Account("1410", "46789-1", 710.00, StatusContaEnum.A));
        accounts.add(new Account("2410", "46789-2", 720.00, StatusContaEnum.B));
        accounts.add(new Account("3410", "46789-3", 730.00, StatusContaEnum.I));
        accounts.add(new Account(null, "46789-3", 730.00, StatusContaEnum.I));
        accounts.add(new Account("4410", "4678-94", 740.00, StatusContaEnum.P));

        log.info("Get {} accounts mocked", (long) accounts.size());
        return accounts;
    }

    public void saveAccountsOnFile(String originalFilename, Set<Account> accountsResult) throws BusinessException {
        if (originalFilename == null || originalFilename.isEmpty()) throw new NullPointerException();

        File file = new File(originalFilename.replace(".csv", "").concat("_sincronizado.csv"));
        try {
            file.createNewFile();
        } catch (IOException e) {
            throw new BusinessException(ERROR_CREATING_NEW_FILE);
        }

        try (FileOutputStream fileOutputStream = new FileOutputStream(file);
             BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream))) {

            log.info("Writing on file {}", file.getAbsoluteFile());
            bufferedWriter.write(String.format(CSV_FIELDS_FORMAT, FIELD_AGENCY, FIELD_ACCOUNT, FIELD_BALANCE, FIELD_STATUS, FIELD_SYNC_RESULT));

            final DecimalFormat brazilCurrency = new DecimalFormat("#,##0.00");
            accountsResult.forEach(account -> {
                try {
                    bufferedWriter.newLine();
                    bufferedWriter.write(String.format(CSV_FIELDS_FORMAT,
                            account.getAgency(),
                            account.getAccount(),
                            brazilCurrency.format(account.getBalance()),
                            account.getStatus(),
                            account.getResult()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            log.info("Saved on file: {}", file.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException(ERROR_SAVING_FILE);
        }
    }

    private Function<String, Account> mapToAccount = (line) -> {
        String[] p = line.split(CSV_FIELD_SEPARATOR);
        return new Account(p[0], p[1], Double.parseDouble(p[2].replace(",", ".")), StatusContaEnum.valueOf(p[3]));
    };

}
