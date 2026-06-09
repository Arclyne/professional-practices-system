package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
class SelfEvaluationManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private SelfEvaluationManager manager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    private SelfEvaluation buildValidEvaluation() {
        SelfEvaluation evaluation = new SelfEvaluation();
        evaluation.setPractitionerId(123);
        evaluation.setReportId(2);
        evaluation.setEvidence("http://pdf.url/documento.pdf");
        evaluation.setQ1(5);
        evaluation.setQ2(5);
        evaluation.setQ3(5);
        evaluation.setQ4(5);
        evaluation.setQ5(5);
        evaluation.setQ6(5);
        evaluation.setQ7(5);
        evaluation.setQ8(5);
        evaluation.setQ9(5);
        evaluation.setQ10(5);
        return evaluation;
    }

    @Test
    void registerSelfEvaluation_ValidData_DoesNotThrow() {
        SelfEvaluation evaluation = buildValidEvaluation();

        assertDoesNotThrow(() -> manager.registerSelfEvaluation(evaluation));
    }

    @Test
    void registerSelfEvaluation_ScoreAboveMaximum_ThrowsManagerException() {
        SelfEvaluation evaluation = buildValidEvaluation();
        evaluation.setQ1(6);

        assertThrows(ManagerException.class, () -> manager.registerSelfEvaluation(evaluation));
    }

    @Test
    void registerSelfEvaluation_ScoreBelowMinimum_ThrowsManagerException() {
        SelfEvaluation evaluation = buildValidEvaluation();
        evaluation.setQ3(0);

        assertThrows(ManagerException.class, () -> manager.registerSelfEvaluation(evaluation));
    }

    @Test
    void registerSelfEvaluation_NullEvidence_ThrowsManagerException() {
        SelfEvaluation evaluation = buildValidEvaluation();
        evaluation.setEvidence(null);

        assertThrows(ManagerException.class, () -> manager.registerSelfEvaluation(evaluation));
    }

    @Test
    void registerSelfEvaluation_InvalidPractitionerId_ThrowsManagerException() {
        SelfEvaluation evaluation = buildValidEvaluation();
        evaluation.setPractitionerId(0);

        assertThrows(ManagerException.class, () -> manager.registerSelfEvaluation(evaluation));
    }

    @Test
    void registerSelfEvaluation_BlankEvidence_ThrowsManagerException() {
        SelfEvaluation evaluation = buildValidEvaluation();
        evaluation.setEvidence("   ");

        assertThrows(ManagerException.class, () -> manager.registerSelfEvaluation(evaluation));
    }

    @Test
    void submitEvidence_ValidUrl_DoesNotThrow() {
        assertDoesNotThrow(() -> manager.submitEvidence(1, "http://documento-final.pdf"));
    }

    @Test
    void submitEvidence_NullUrl_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> manager.submitEvidence(1, null));
    }

    @Test
    void submitEvidence_BlankUrl_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> manager.submitEvidence(1, "   "));
    }

    @Test
    void markAsReviewed_ExistingEvaluation_DoesNotThrow() {
        assertDoesNotThrow(() -> manager.markAsReviewed(1));
    }

    @Test
    void updateStatus_ValidStatus_DoesNotThrow() {
        assertDoesNotThrow(() -> manager.updateStatus(1, "Pendiente"));
    }

    @Test
    void recoverSelfEvaluation_ExistingReportId_ReturnsEvaluation() throws ManagerException {
        SelfEvaluation result = manager.recoverSelfEvaluation(1);

        assertNotNull(result);
    }

    @Test
    void recoverSelfEvaluation_NonExistentReportId_ReturnsEmptyObject() throws ManagerException {
        SelfEvaluation result = manager.recoverSelfEvaluation(999);

        assertEquals(new SelfEvaluation(), result);
    }
}