package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.dto.SelfEvaluation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class SelfEvaluationDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int STORED_EVALUATION_ID = 1;
    private static final int NON_EXISTENT_ID = 9999;
    private static final String STORED_EVIDENCE_URL = "https://storage.uv.mx/evidencias/autoevaluacion_zS24242424.pdf";

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private ISelfEvaluationDAO selfEvaluationDAO;

    private SelfEvaluation validEvaluation;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        validEvaluation = new SelfEvaluation();
        validEvaluation.setQ1(5);
        validEvaluation.setQ2(4);
        validEvaluation.setQ3(5);
        validEvaluation.setQ4(4);
        validEvaluation.setQ5(5);
        validEvaluation.setQ6(4);
        validEvaluation.setQ7(5);
        validEvaluation.setQ8(4);
        validEvaluation.setQ9(5);
        validEvaluation.setQ10(4);
        validEvaluation.setEvidence("https://storage.uv.mx/evidencias/autoevaluacion_final_zS24242424.pdf");
        validEvaluation.setPractitionerId(PRACTITIONER_ID);
        validEvaluation.setReportId(2);
        validEvaluation.setStatus("Pendiente");
    }

    @Test
    void insertSelfEvaluation_ValidEvaluation_ReturnsGeneratedId() throws DAOException {
        int resultId = selfEvaluationDAO.insertSelfEvaluation(validEvaluation);

        assertTrue(resultId > 0);
    }

    @Test
    void getSelfEvaluationByReportId_ExistingReport_ReturnsEvaluation() throws DAOException {
        SelfEvaluation expectedEvaluation = new SelfEvaluation();
        expectedEvaluation.setSelfEvalId(STORED_EVALUATION_ID);
        expectedEvaluation.setQ1(5);
        expectedEvaluation.setQ2(5);
        expectedEvaluation.setQ3(5);
        expectedEvaluation.setQ4(5);
        expectedEvaluation.setQ5(5);
        expectedEvaluation.setQ6(5);
        expectedEvaluation.setQ7(5);
        expectedEvaluation.setQ8(5);
        expectedEvaluation.setQ9(5);
        expectedEvaluation.setQ10(5);
        expectedEvaluation.setEvidence(STORED_EVIDENCE_URL);
        expectedEvaluation.setPractitionerId(PRACTITIONER_ID);
        expectedEvaluation.setReportId(1);
        expectedEvaluation.setStatus("Pendiente");

        SelfEvaluation recoveredEvaluation = selfEvaluationDAO.getSelfEvaluationByReportId(1);

        assertEquals(expectedEvaluation, recoveredEvaluation);
    }

    @Test
    void updateSelfEvaluation_ValidModifiedData_DoesNotThrow() throws DAOException {
        validEvaluation.setStatus("Revisada");
        validEvaluation.setEvidence("https://storage.uv.mx/evidencias/autoevaluacion_corregida_zS24242424.pdf");

        assertDoesNotThrow(() -> selfEvaluationDAO.updateSelfEvaluation(validEvaluation, STORED_EVALUATION_ID));
    }

    @Test
    void updateSelfEvaluationStatus_ValidStatus_DoesNotThrow() throws DAOException {
        assertDoesNotThrow(() -> selfEvaluationDAO.updateSelfEvaluationStatus(STORED_EVALUATION_ID, "Revisada"));
    }

    @Test
    void updateSelfEvaluationEvidence_ValidEvidence_DoesNotThrow() throws DAOException {
        assertDoesNotThrow(() -> selfEvaluationDAO.updateSelfEvaluationEvidence(STORED_EVALUATION_ID,
                "https://storage.uv.mx/evidencias/evidencia_actualizada_zS24242424.pdf"));
    }

    @Test
    void insertSelfEvaluation_NonExistentReport_ThrowsDAOException() {
        validEvaluation.setReportId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> selfEvaluationDAO.insertSelfEvaluation(validEvaluation));
    }

    @Test
    void getSelfEvaluationByReportId_NonExistentReport_ReturnsNull() throws DAOException {
        SelfEvaluation recoveredEvaluation = selfEvaluationDAO.getSelfEvaluationByReportId(NON_EXISTENT_ID);

        assertNull(recoveredEvaluation);
    }

    @Test
    void updateSelfEvaluation_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> selfEvaluationDAO.updateSelfEvaluation(validEvaluation, NON_EXISTENT_ID));
    }

    @Test
    void updateSelfEvaluationStatus_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> selfEvaluationDAO.updateSelfEvaluationStatus(NON_EXISTENT_ID, "Revisada"));
    }

    @Test
    void updateSelfEvaluationEvidence_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> selfEvaluationDAO.updateSelfEvaluationEvidence(NON_EXISTENT_ID,
                "https://storage.uv.mx/evidencias/evidencia_huerfana.pdf"));
    }
}
