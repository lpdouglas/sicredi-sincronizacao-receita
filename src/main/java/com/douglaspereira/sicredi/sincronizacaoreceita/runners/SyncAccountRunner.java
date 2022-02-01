package com.douglaspereira.sicredi.sincronizacaoreceita.runners;

import com.douglaspereira.sicredi.sincronizacaoreceita.exceptions.BusinessException;
import com.douglaspereira.sicredi.sincronizacaoreceita.services.SyncAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

@Component
public class SyncAccountRunner implements ApplicationRunner {

    public static final String FILENAME_NOT_PASSED_ON_ARGS = "Filename not passed on args";
    private static final String ERROR_CREATING_NEW_FILE = "Error creating new file";
    public static final String FILE_ALREADY_EXISTS = "File already exists";
    private final SyncAccountService syncAccountService;
    private final Logger log;

    public SyncAccountRunner(SyncAccountService syncAccountService) {
        this.syncAccountService = syncAccountService;
        this.log = LoggerFactory.getLogger(SyncAccountRunner.class);
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            File file = getFileFromArgs(args);
            File targetFile = createTargetFile(file);

            syncAccountService.syncAccountsFromFile(file, targetFile);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private File getFileFromArgs(ApplicationArguments args) throws Exception {
        String filename;
        if (!args.getNonOptionArgs().isEmpty()) {
            filename = args.getNonOptionArgs().get(0);
        } else {
            throw new BusinessException(FILENAME_NOT_PASSED_ON_ARGS);
        }
        File file = new File(filename);
        if (!file.isFile()) throw new FileNotFoundException();

        return file;
    }


    private File createTargetFile(File file) {
        File targetFile = new File(file.getAbsolutePath().replace(".csv", "").concat("_sincronizado.csv"));
        try {
            boolean fileCreated = targetFile.createNewFile();
            if (!fileCreated) log.warn(FILE_ALREADY_EXISTS);

            return targetFile;
        } catch (IOException e) {
            throw new BusinessException(ERROR_CREATING_NEW_FILE);
        }
    }

}
