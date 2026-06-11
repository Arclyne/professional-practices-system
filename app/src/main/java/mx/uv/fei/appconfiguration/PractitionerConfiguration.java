package mx.uv.fei.appconfiguration;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Provide;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.domain.common.CsvPractitionerParser;
import mx.uv.fei.domain.common.IFileBackup;
import mx.uv.fei.domain.common.IPractitionerParser;
import mx.uv.fei.domain.common.LocalCsvBackup;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Keep
public class PractitionerConfiguration {

    private static final String TEST_PROFILE = "test";
    private static final Path PRODUCTION_BACKUP_DIRECTORY = Paths.get("app", "documents", "batches");
    private static final Path TEST_BACKUP_DIRECTORY = Paths.get("target", "test-batches");

    private final String activeProfile;

    @Inject
    public PractitionerConfiguration(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    @Provide
    public IFileBackup provideFileBackup() {
        return new LocalCsvBackup(resolveBackupDirectory());
    }

    @Provide
    public IPractitionerParser providePractitionerParser() {
        return new CsvPractitionerParser();
    }

    private Path resolveBackupDirectory() {
        Path backupDirectory = PRODUCTION_BACKUP_DIRECTORY;

        if (TEST_PROFILE.equals(activeProfile)) {
            backupDirectory = TEST_BACKUP_DIRECTORY;
        }

        return backupDirectory;
    }
}
