package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;
import java.sql.SQLException;
import java.util.List;
import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PostulationManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private PostulationManager postulationManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void retrievePractitionerPostulations_ValidPractitioner_ReturnsList() throws ManagerException {
        assertNotNull(postulationManager.retrievePractitionerPostulations(123));
    }

    @Test
    void registerPractitionerPriorities_EmptyList_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> postulationManager.registerPractitionerPriorities(123, List.of()));
    }
}