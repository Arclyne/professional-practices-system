package mx.uv.fei.domain.manager.evaluation;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.sql.SQLException;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.enums.SelfEvaluationStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class SelfEvaluationManagerTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int STORED_EVALUATION_ID = 1;
    private static final String STORED_EVIDENCE_URL = "https://storage.uv.mx/evidencias/autoevaluacion_zS24242424.pdf";

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private SelfEvaluationManager selfEvaluationManager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    private SelfEvaluation buildValidEvaluation() {
        SelfEvaluation validEvaluation = new SelfEvaluation();
        validEvaluation.setPractitionerId(PRACTITIONER_ID);
        validEvaluation.setReportId(2);
        validEvaluation.setEvidence("https://storage.uv.mx/evidencias/autoevaluacion_final_zS24242424.pdf");
        validEvaluation.setQ1(5);
        validEvaluation.setQ2(5);
        validEvaluation.setQ3(5);
        validEvaluation.setQ4(5);
        validEvaluation.setQ5(5);
        validEvaluation.setQ6(5);
        validEvaluation.setQ7(5);
        validEvaluation.setQ8(5);
        validEvaluation.setQ9(5);
        validEvaluation.setQ10(5);
        return validEvaluation;
    }

    @Test
    void registerSelfEvaluation_ValidData_DoesNotThrow() {
        SelfEvaluation validEvaluation = buildValidEvaluation();

        assertDoesNotThrow(() -> selfEvaluationManager.registerSelfEvaluation(validEvaluation));
    }

    @Test
    void registerSelfEvaluation_ScoreAboveMaximum_ThrowsManagerException() {
        SelfEvaluation invalidEvaluation = buildValidEvaluation();
        invalidEvaluation.setQ1(6);

        assertThrows(ManagerException.class, () -> selfEvaluationManager.registerSelfEvaluation(invalidEvaluation));
    }

    @Test
    void registerSelfEvaluation_ScoreBelowMinimum_ThrowsManagerException() {
        SelfEvaluation invalidEvaluation = buildValidEvaluation();
        invalidEvaluation.setQ3(0);

        assertThrows(ManagerException.class, () -> selfEvaluationManager.registerSelfEvaluation(invalidEvaluation));
    }

    @Test
    void registerSelfEvaluation_NullEvidence_ThrowsManagerException() {
        SelfEvaluation invalidEvaluation = buildValidEvaluation();
        invalidEvaluation.setEvidence(null);

        assertThrows(ManagerException.class, () -> selfEvaluationManager.registerSelfEvaluation(invalidEvaluation));
    }

    @Test
    void registerSelfEvaluation_InvalidPractitionerId_ThrowsManagerException() {
        SelfEvaluation invalidEvaluation = buildValidEvaluation();
        invalidEvaluation.setPractitionerId(0);

        assertThrows(ManagerException.class, () -> selfEvaluationManager.registerSelfEvaluation(invalidEvaluation));
    }

    @Test
    void registerSelfEvaluation_BlankEvidence_ThrowsManagerException() {
        SelfEvaluation invalidEvaluation = buildValidEvaluation();
        invalidEvaluation.setEvidence("   ");

        assertThrows(ManagerException.class, () -> selfEvaluationManager.registerSelfEvaluation(invalidEvaluation));
    }

    @Test
    void submitEvidence_ValidUrl_DoesNotThrow() {
        assertDoesNotThrow(() -> selfEvaluationManager.submitEvidence(STORED_EVALUATION_ID,
                "https://storage.uv.mx/evidencias/evidencia_actualizada_zS24242424.pdf"));
    }

    @Test
    void submitEvidence_NullUrl_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> selfEvaluationManager.submitEvidence(STORED_EVALUATION_ID, null));
    }

    @Test
    void submitEvidence_BlankUrl_ThrowsManagerException() {
        assertThrows(ManagerException.class, () -> selfEvaluationManager.submitEvidence(STORED_EVALUATION_ID, "   "));
    }

    @Test
    void markAsReviewed_ExistingEvaluation_DoesNotThrow() {
        assertDoesNotThrow(() -> selfEvaluationManager.markAsReviewed(STORED_EVALUATION_ID));
    }

    @Test
    void updateSelfEvaluationStatus_ValidStatus_DoesNotThrow() {
        assertDoesNotThrow(() -> selfEvaluationManager.updateSelfEvaluationStatus(STORED_EVALUATION_ID, "Pendiente"));
    }

    @Test
    void rejectSelfEvaluation_ValidComment_DoesNotThrow() {
        assertDoesNotThrow(() -> selfEvaluationManager.rejectSelfEvaluation(STORED_EVALUATION_ID,
                "Las respuestas no coinciden con la evidencia entregada."));
    }

    @Test
    void rejectSelfEvaluation_BlankComment_ThrowsManagerException() {
        assertThrows(ManagerException.class,
                () -> selfEvaluationManager.rejectSelfEvaluation(STORED_EVALUATION_ID, "   "));
    }

    @Test
    void rejectSelfEvaluation_ValidComment_PersistsRejectedStatusWithComment() throws ManagerException {
        String rejectionComment = "Las respuestas no coinciden con la evidencia entregada.";

        selfEvaluationManager.rejectSelfEvaluation(STORED_EVALUATION_ID, rejectionComment);
        SelfEvaluation rejectedEvaluation = selfEvaluationManager.recoverSelfEvaluation(1);

        assertEquals(SelfEvaluationStatus.REJECTED.getDatabaseValue(), rejectedEvaluation.getStatus());
        assertEquals(rejectionComment, rejectedEvaluation.getReviewComment());
    }

    @Test
    void recoverSelfEvaluation_ExistingReportId_ReturnsExpectedEvaluation() throws ManagerException {
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

        SelfEvaluation recoveredEvaluation = selfEvaluationManager.recoverSelfEvaluation(1);

        assertEquals(expectedEvaluation, recoveredEvaluation);
    }

    @Test
    void recoverSelfEvaluation_NonExistentReportId_ReturnsNull() throws ManagerException {
        SelfEvaluation recoveredEvaluation = selfEvaluationManager.recoverSelfEvaluation(999);

        assertNull(recoveredEvaluation);
    }
}
