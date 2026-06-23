package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class CloudStorageManagerTest {

    @Inject
    private CloudStorageManager cloudStorageManager;

    @Test
    void uploadEvidenceFile_ValidFile_ReturnsStringUrl() throws IOException, ManagerException {
        File evidenceFile = Files.createTempFile("evidencia_practicas", ".pdf").toFile();
        evidenceFile.deleteOnExit();
        Files.writeString(evidenceFile.toPath(), "Contenido de evidencia de prueba.");

        String evidenceUrl = cloudStorageManager.uploadEvidenceFile(evidenceFile);

        assertNotNull(evidenceUrl);
    }

    @Test
    void uploadEvidenceFile_NullFile_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> cloudStorageManager.uploadEvidenceFile(null));
    }
}
