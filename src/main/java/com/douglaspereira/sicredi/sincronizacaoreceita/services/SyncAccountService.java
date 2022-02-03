package com.douglaspereira.sicredi.sincronizacaoreceita.services;

import java.io.File;

public interface SyncAccountService {
    void syncAccountsFromFile(File accountsFile, File targetFile);
}
