package mx.uv.fei.domain.common;

import java.io.File;
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

    @Override
    public void backupFile(File sourceFile, String userIdentifier) throws ManagerException {
        try {
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            String originalFileName = sourceFile.getName().replace(".csv", "");
            String sanitizedUserIdentifier = userIdentifier.replaceAll("[^a-zA-Z0-9.-]", "_");

            String backupFileName = currentDate + "_" + originalFileName + "_" + sanitizedUserIdentifier + ".csv";

            Path backupDirectory = Paths.get("app", "documents", "batches");
            if (!Files.exists(backupDirectory)) {
                Files.createDirectories(backupDirectory);
            }

            Path backupPath = backupDirectory.resolve(backupFileName);
            Files.copy(sourceFile.toPath(), backupPath, StandardCopyOption.REPLACE_EXISTING);

        } catch (Exception exception) {
            throw new ManagerException("No se pudo respaldar el archivo.");
        }
    }
}