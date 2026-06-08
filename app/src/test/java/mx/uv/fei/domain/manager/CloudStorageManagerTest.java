package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.nio.file.Files;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class CloudStorageManagerTest {

    @Inject private CloudStorageManager cloudStorageManager;

    @Test
    void uploadEvidenceFile_ValidFile_ReturnsStringUrl() throws Exception {
        File tempFile = Files.createTempFile("test_evidence", ".pdf").toFile();
        tempFile.deleteOnExit();

        String url = cloudStorageManager.uploadEvidenceFile(tempFile);
        assertNotNull(url);
        assertTrue(url.contains("SimuladorOneDrive"));
    }

    @Test
    void uploadEvidenceFile_NullFile_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> cloudStorageManager.uploadEvidenceFile(null));
    }
}