package mx.uv.fei.domain.common;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.Test;

class PersistenceErrorTranslatorTest {

    private static DAOException wrapInChain(SQLException sqlCause) {
        // Imita el envoltorio real: BaseDAO/DAO anidan la SQLException dentro de varias DAOException.
        return new DAOException("Error al insertar (rollback).", new DAOException("Error al insertar el usuario.", sqlCause));
    }

    @Test
    void translate_DuplicateUsernameDeepInChain_ReturnsInUseMessage() {
        DAOException failure = wrapInChain(
                new SQLIntegrityConstraintViolationException("Duplicate entry '30011111' for key 'user.username'"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        assertTrue(result.getMessage().toLowerCase().contains("uso"), result.getMessage());
    }

    @Test
    void translate_DuplicateEmail_ReturnsEmailMessage() {
        DAOException failure = wrapInChain(
                new SQLIntegrityConstraintViolationException("Unique index violation on PUBLIC.USER(EMAIL)"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        assertTrue(result.getMessage().toLowerCase().contains("correo"), result.getMessage());
    }

    @Test
    void translate_ActiveAdministratorGuard_ReturnsActiveAdminMessage() {
        DAOException failure = wrapInChain(new SQLIntegrityConstraintViolationException(
                "Unique index violation: UQ_USER_ACTIVE_SINGLETON_ROLE ON PUBLIC.USER(ACTIVE_SINGLETON_ROLE) VALUES ('Administrator')"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        String message = result.getMessage().toLowerCase();
        assertTrue(message.contains("administrador") && message.contains("activo"), result.getMessage());
    }

    @Test
    void translate_ActiveCoordinatorGuard_ReturnsActiveCoordinatorMessage() {
        DAOException failure = wrapInChain(new SQLIntegrityConstraintViolationException(
                "Duplicate entry 'Coordinator' for key 'user.uq_user_active_singleton_role'"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        String message = result.getMessage().toLowerCase();
        assertTrue(message.contains("coordinador") && message.contains("activo"), result.getMessage());
    }

    @Test
    void translate_DuplicateDocumentType_ReturnsDocumentMessage() {
        DAOException failure = wrapInChain(new SQLIntegrityConstraintViolationException(
                "Unique index violation: UQ_PRACTITIONER_DOCUMENT_TYPE ON PUBLIC.PRACTITIONER_DOCUMENT(PRACTITIONER_ID, DOCUMENT_TYPE_ID)"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        String message = result.getMessage().toLowerCase();
        assertTrue(message.contains("documento") && !message.contains("constraint"), result.getMessage());
    }

    @Test
    void translate_DuplicateGradePeriod_ReturnsGradeMessage() {
        DAOException failure = wrapInChain(new SQLIntegrityConstraintViolationException(
                "Duplicate entry '123-Junio-Diciembre 2026' for key 'practitioner_grade.uq_grade_period'"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        String message = result.getMessage().toLowerCase();
        assertTrue(message.contains("calificación") && !message.contains("duplicate"), result.getMessage());
    }

    @Test
    void translate_DuplicateSelfEvaluationReport_ReturnsSelfEvaluationMessage() {
        DAOException failure = wrapInChain(new SQLIntegrityConstraintViolationException(
                "Unique index violation: UQ_SELFEVAL_REPORT ON PUBLIC.SELF_EVALUATION(REPORT_ID)"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        String message = result.getMessage().toLowerCase();
        assertTrue(message.contains("autoevaluación") && !message.contains("constraint"), result.getMessage());
    }

    @Test
    void translate_NonIntegrityFailure_ReturnsGenericConnectionMessage() {
        DAOException failure = new DAOException("Fallo de red.", new SQLException("Connection reset"));

        ManagerException result = PersistenceErrorTranslator.translate(failure);

        assertTrue(result.getMessage().toLowerCase().contains("conexión"), result.getMessage());
    }

    @Test
    void isDuplicateEntry_IntegrityViolationInChain_ReturnsTrue() {
        DAOException failure = wrapInChain(new SQLIntegrityConstraintViolationException("Duplicate entry"));

        assertTrue(PersistenceErrorTranslator.isDuplicateEntry(failure));
    }

    @Test
    void isDuplicateEntry_PlainSqlException_ReturnsFalse() {
        DAOException failure = new DAOException("Fallo de red.", new SQLException("timeout"));

        assertFalse(PersistenceErrorTranslator.isDuplicateEntry(failure));
    }
}
