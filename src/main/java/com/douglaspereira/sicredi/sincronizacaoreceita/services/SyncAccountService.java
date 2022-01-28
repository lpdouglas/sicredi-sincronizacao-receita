package com.douglaspereira.sicredi.sincronizacaoreceita.services;

import com.douglaspereira.sicredi.sincronizacaoreceita.enums.StatusContaEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.enums.SyncResultEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.external.ReceitaService;
import com.douglaspereira.sicredi.sincronizacaoreceita.pojos.Account;
import com.douglaspereira.sicredi.sincronizacaoreceita.runners.SyncAccountRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class SyncAccountService {

    Logger log = LoggerFactory.getLogger(SyncAccountRunner.class);
    private static final String FIELD_ACCOUNT = "conta";
    private static final String FIELD_AGENCY = "agencia";
    private static final String FIELD_BALANCE = "saldo";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_SYNC_RESULT = "resultado";

    public Set<Account> syncAccounts(ReceitaService receitaService, Set<Account> accounts) {
        log.info("Sync Accounts: Started");
        Set<Account> accountsResult = accounts.parallelStream().map(contaDTO -> {
            try {
                boolean isContaAtualizada = receitaService.atualizarConta(contaDTO.getAgency(), contaDTO.getAccount(),
                        contaDTO.getBalance(), contaDTO.getStatus().name());
                contaDTO.setResult(isContaAtualizada ? SyncResultEnum.SYNCRONIZED : SyncResultEnum.NOT_SYNCRONIZED);

                return contaDTO;
            } catch (InterruptedException | RuntimeException e) {
                contaDTO.setResult(SyncResultEnum.ERROR);
                return contaDTO;
            } finally {
                log.info("synchronized account {} from agency {}, Result: {}", contaDTO.getAccount(), contaDTO.getAgency(), contaDTO.getResult());
            }
        }).collect(Collectors.toSet());
        log.info("Sync Accounts: Ended");
        return accountsResult;
    }

    public Set<Account> getAccountsFromFile(String file) throws IOException {
        try (InputStream fileInputStream = new FileInputStream(new File(file));
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {

            return bufferedReader.lines()
                    .skip(1)
                    .map(mapToAccount)
                    .collect(Collectors.toSet());
        }
    }

    private Set<Account> getAccountsFromMock() {
        Set<Account> accounts = new HashSet<>();
        accounts.add(new Account("0101", "123456", 100.50, StatusContaEnum.A));
        accounts.add(new Account("0202", "234567", 200.40, StatusContaEnum.B));
        accounts.add(new Account("0301", "345678", 300.30, StatusContaEnum.I));
        accounts.add(new Account("0301", "34NOT78", 300.30, StatusContaEnum.I));
        accounts.add(new Account("0403", "456789", 400.20, StatusContaEnum.P));
        accounts.add(new Account("0410", "467890", 700.00, StatusContaEnum.B));
        accounts.add(new Account("1410", "467891", 710.00, StatusContaEnum.A));
        accounts.add(new Account("2410", "467892", 720.00, StatusContaEnum.B));
        accounts.add(new Account("3410", "467893", 730.00, StatusContaEnum.I));
        accounts.add(new Account(null, "467893", 730.00, StatusContaEnum.I));
        accounts.add(new Account("4410", "467894", 740.00, StatusContaEnum.P));

        log.info("Generating file with {} accounts processed", (long) accounts.size());
        return accounts;
    }

    public void saveAccountsOnFile(String file, Set<Account> accountsResult) throws IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(new File(file));

        String csvString = "%s;%s;%s;%s;%s";
        try (BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(fileOutputStream))) {
            bufferedWriter.write(String.format(csvString, FIELD_ACCOUNT, FIELD_AGENCY, FIELD_BALANCE, FIELD_STATUS, FIELD_SYNC_RESULT));

            accountsResult.forEach(account -> {
                try {
                    bufferedWriter.newLine();
                    bufferedWriter.write(csvString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
    }

    public static Function<String, Account> mapToAccount = (line) -> {
        String[] p = line.split(";");
        return new Account(p[0], p[1], Double.parseDouble(p[2]), StatusContaEnum.valueOf(p[3]));
    };

}
