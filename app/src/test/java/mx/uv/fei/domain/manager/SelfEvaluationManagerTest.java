package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.*;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

@StartEtiquetteTest
@Profile("test")
class SelfEvaluationManagerTest {

    @Inject private IDatabaseConnection dbConnection;
    @Inject private SelfEvaluationManager manager;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);
    }

    @Test
    void registerSelfEvaluation_ValidData_Success() {
        SelfEvaluation eval = new SelfEvaluation();
        eval.setPractitionerId(123);
        eval.setReportId(2);
        eval.setQ1(5); eval.setQ2(5); eval.setQ3(5); eval.setQ4(5); eval.setQ5(5);
        eval.setQ6(5); eval.setQ7(5); eval.setQ8(5); eval.setQ9(5); eval.setQ10(5);
        eval.setEvidence("http://pdf.url/documento.pdf");

        assertDoesNotThrow(() -> manager.registerSelfEvaluation(eval));
    }

    @Test
    void registerSelfEvaluation_InvalidScore_ThrowsManagerException() {
        SelfEvaluation eval = new SelfEvaluation();
        eval.setPractitionerId(123);
        eval.setReportId(2);
        eval.setQ1(6);
        eval.setEvidence("http://pdf.url/documento.pdf");

        assertThrows(ManagerException.class, () -> manager.registerSelfEvaluation(eval));
    }

    @Test
    void markAsReviewed_ExistingEval_Success() {
        assertDoesNotThrow(() -> manager.markAsReviewed(1));
    }

    @Test
    void submitEvidence_ValidUrl_Success() {
        assertDoesNotThrow(() -> manager.submitEvidence(1, "http://documento-final.pdf"));
    }
}