package mx.uv.fei.domain.common;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class LocalCsvBackup implements IFileBackup {

    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String CSV_EXTENSION = ".csv";
    private static final String EMPTY_STRING = "";
    private static final String IDENTIFIER_REGEX = "[^a-zA-Z0-9.-]";
    private static final String UNDERSCORE = "_";
    private static final Path BACKUP_DIRECTORY = Paths.get("app", "documents", "batches");

    @Override
    public void backupFile(File sourceFile, String userIdentifier) throws ManagerException {
        try {
            ensureBackupDirectoryExists();
            Path backupFilePath = BACKUP_DIRECTORY.resolve(buildBackupFileName(sourceFile, userIdentifier));
            Files.copy(sourceFile.toPath(), backupFilePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException _) {
            throw new ManagerException("No se pudo respaldar el archivo.");
        }
    }

    private String buildBackupFileName(File sourceFile, String userIdentifier) {
        String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
        String originalFileName = sourceFile.getName().replace(CSV_EXTENSION, EMPTY_STRING);
        String sanitizedUserIdentifier = userIdentifier.replaceAll(IDENTIFIER_REGEX, UNDERSCORE);
        return currentDate + UNDERSCORE + originalFileName + UNDERSCORE + sanitizedUserIdentifier + CSV_EXTENSION;
    }

    private void ensureBackupDirectoryExists() throws IOException {
        if (!Files.exists(BACKUP_DIRECTORY)) {
            Files.createDirectories(BACKUP_DIRECTORY);
        }
    }
}