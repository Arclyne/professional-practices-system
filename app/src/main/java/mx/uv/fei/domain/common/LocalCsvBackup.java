package mx.uv.fei.domain.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class LocalCsvBackup implements IFileBackup {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String CSV_EXTENSION = ".csv";
    private static final String EMPTY_STRING = "";
    private static final String IDENTIFIER_REGEX = "[^a-zA-Z0-9.-]";
    private static final String UNDERSCORE = "_";
    private static final String DIR_APP = "app";
    private static final String DIR_DOCUMENTS = "documents";
    private static final String DIR_BATCHES = "batches";
    private static final String BACKUP_ERROR_MESSAGE = "No se pudo respaldar el archivo.";

    @Override
    public void backupFile(File sourceFile, String userIdentifier) throws ManagerException {
        try {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            String originalFileName = sourceFile.getName().replace(CSV_EXTENSION, EMPTY_STRING);
            String sanitizedUserIdentifier = userIdentifier.replaceAll(IDENTIFIER_REGEX, UNDERSCORE);

            String backupFileName = currentDate + UNDERSCORE + originalFileName + UNDERSCORE + sanitizedUserIdentifier + CSV_EXTENSION;

            Path backupDirectory = Paths.get(DIR_APP, DIR_DOCUMENTS, DIR_BATCHES);

            if (!Files.exists(backupDirectory)) {
                Files.createDirectories(backupDirectory);
            }

            Path backupPath = backupDirectory.resolve(backupFileName);
            Files.copy(sourceFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (IOException _) {
            throw new ManagerException(BACKUP_ERROR_MESSAGE);
        }
    }
}