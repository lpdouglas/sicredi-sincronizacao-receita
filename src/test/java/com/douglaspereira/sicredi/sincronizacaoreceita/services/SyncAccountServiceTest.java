package com.douglaspereira.sicredi.sincronizacaoreceita.services;

import com.douglaspereira.sicredi.sincronizacaoreceita.enums.StatusContaEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.enums.SyncResultEnum;
import com.douglaspereira.sicredi.sincronizacaoreceita.exceptions.BusinessException;
import com.douglaspereira.sicredi.sincronizacaoreceita.external_api.ReceitaService;
import com.douglaspereira.sicredi.sincronizacaoreceita.pojos.Account;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class SyncAccountServiceTest {

    private SyncAccountServiceImpl syncAccountService;

    @BeforeEach
    void setUp() {
        syncAccountService = new SyncAccountServiceImpl(new ReceitaService());
    }

    @Test
    void syncAccountsFromFile() throws IOException {
        String path = "src/test/resources/";
        File accountFiles = new File(path + "contas-teste.csv");
        File targetFile = new File(path + "contas-teste_sincronizado.csv");
        targetFile.delete();
        syncAccountService.syncAccountsFromFile(accountFiles, targetFile);

        assertTrue(targetFile.isFile());
        Set<Account> accountsResult = getAccountsFromCsvFile(targetFile);
        assertNotNull(accountsResult);
        assertEquals(8, accountsResult.size());
        assertFalse(accountsResult.stream().anyMatch(account -> account.getResult() == null));
    }

    @Test
    void syncAccountsFromFileWithoutFile() {
        String path = "src/test/resources/";
        File accountFiles = new File(path + "contas-teste-nao-existe.csv");
        File targetFile = new File(path + "contas-teste_sincronizado.csv");

        Exception errorType = null;
        try {
            syncAccountService.syncAccountsFromFile(accountFiles, targetFile);
        } catch (Exception e) {
            errorType = e;
            assertEquals("Error maping accounts from file", e.getMessage());
        }

        assert errorType instanceof BusinessException;
    }

    private Set<Account> getAccountsFromCsvFile(File file) throws IOException {
        try (InputStream fileInputStream = new FileInputStream(file);
             BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream))) {

            Set<Account> collectAccounts = bufferedReader.lines()
                    .skip(1)
                    .map(line -> {
                        String[] p = line.split(";");
                        Account account = new Account(p[0], p[1], Double.parseDouble(p[2].replace(",", ".")), StatusContaEnum.valueOf(p[3]));
                        account.setResult(
                                Arrays.stream(SyncResultEnum.values())
                                        .filter(syncResultEnum -> syncResultEnum.toString().contentEquals(p[4])).findFirst().get()
                        );
                        return account;
                    })
                    .collect(Collectors.toSet());

            return collectAccounts;
        }
    }

}