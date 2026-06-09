package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        validEvaluation.setEvidence("http://nueva.url/evidencia.pdf");
        validEvaluation.setPractitionerId(123);
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
        expectedEvaluation.setSelfEvalId(1);
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
        expectedEvaluation.setEvidence("http://pdf.url");
        expectedEvaluation.setPractitionerId(123);
        expectedEvaluation.setReportId(1);
        expectedEvaluation.setStatus("Pendiente");

        SelfEvaluation recovered = selfEvaluationDAO.getSelfEvaluationByReportId(1);

        assertEquals(expectedEvaluation, recovered);
    }

    @Test
    void updateSelfEvaluation_ValidModifiedData_ReturnsTrue() throws DAOException {
        validEvaluation.setStatus("Revisada");
        validEvaluation.setEvidence("http://modificada.url/evidencia.pdf");

        boolean isUpdated = selfEvaluationDAO.updateSelfEvaluation(validEvaluation, 1);

        assertTrue(isUpdated);
    }

    @Test
    void updateStatus_ValidStatus_ReturnsTrue() throws DAOException {
        boolean isUpdated = selfEvaluationDAO.updateStatus(1, "Revisada");

        assertTrue(isUpdated);
    }

    @Test
    void updateEvidence_ValidEvidence_ReturnsTrue() throws DAOException {
        boolean isUpdated = selfEvaluationDAO.updateEvidence(1, "http://nueva-evidencia.url");

        assertTrue(isUpdated);
    }

    @Test
    void insertSelfEvaluation_NonExistentReport_ThrowsDAOException() {
        validEvaluation.setReportId(9999);

        assertThrows(DAOException.class, () -> {
            selfEvaluationDAO.insertSelfEvaluation(validEvaluation);
        });
    }

    @Test
    void getSelfEvaluationByReportId_NonExistentReport_ReturnsEmptyEvaluation() throws DAOException {
        SelfEvaluation recovered = selfEvaluationDAO.getSelfEvaluationByReportId(9999);

        assertEquals(new SelfEvaluation(), recovered);
    }

    @Test
    void updateSelfEvaluation_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = selfEvaluationDAO.updateSelfEvaluation(validEvaluation, 9999);

        assertFalse(isUpdated);
    }

    @Test
    void updateStatus_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = selfEvaluationDAO.updateStatus(9999, "Revisada");

        assertFalse(isUpdated);
    }

    @Test
    void updateEvidence_NonExistentId_ReturnsFalse() throws DAOException {
        boolean isUpdated = selfEvaluationDAO.updateEvidence(9999, "http://url");

        assertFalse(isUpdated);
    }
}