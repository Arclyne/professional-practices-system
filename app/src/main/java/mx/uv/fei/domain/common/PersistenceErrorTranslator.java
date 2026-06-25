package mx.uv.fei.domain.common;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.sql.SQLIntegrityConstraintViolationException;

/**
 * Traduce fallos de persistencia ({@link DAOException}) a {@link ManagerException} con un mensaje
 * claro para el usuario.
 *
 * <p>La detección de duplicados se basa en el <em>tipo</em> de la excepción SQL subyacente
 * ({@link SQLIntegrityConstraintViolationException}), recorriendo toda la cadena de causas, y no en
 * el texto del mensaje. Esto cierra la condición de carrera del patrón "SELECT y luego INSERT": aun
 * cuando dos operaciones simultáneas pasen la verificación previa, la restricción {@code UNIQUE} de
 * la base de datos rechaza el segundo INSERT y aquí se reporta como "ya está en uso" en lugar de un
 * genérico error de conexión.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public final class PersistenceErrorTranslator {

    private static final String GENERIC_CONNECTION_MESSAGE =
            "Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.";
    private static final String GENERIC_DUPLICATE_MESSAGE =
            "Ya existe un registro con los datos proporcionados. Verifique e intente con otros.";

    private PersistenceErrorTranslator() {
    }

    /**
     * Convierte un fallo de persistencia en una {@link ManagerException} con mensaje apto para el
     * usuario: un mensaje específico de duplicidad si la causa es una violación de unicidad, o un
     * mensaje genérico de conexión en cualquier otro caso.
     */
    public static ManagerException translate(DAOException persistenceFailure) {
        SQLIntegrityConstraintViolationException duplicate = findIntegrityViolation(persistenceFailure);
        String message = duplicate != null ? describeDuplicate(duplicate) : GENERIC_CONNECTION_MESSAGE;
        return new ManagerException(message, persistenceFailure);
    }

    /**
     * Indica si el fallo proviene de una violación de restricción de unicidad (entrada duplicada).
     */
    public static boolean isDuplicateEntry(Throwable error) {
        return findIntegrityViolation(error) != null;
    }

    private static SQLIntegrityConstraintViolationException findIntegrityViolation(Throwable error) {
        Throwable cause = error;

        while (cause != null) {
            if (cause instanceof SQLIntegrityConstraintViolationException violation) {
                return violation;
            }
            cause = cause.getCause();
        }

        return null;
    }

    private static String describeDuplicate(SQLIntegrityConstraintViolationException violation) {
        String detail = violation.getMessage() != null ? violation.getMessage().toLowerCase() : "";

        if (detail.contains("active_period_guard") || detail.contains("uq_school_period_active")) {
            return "Ya existe un periodo activo. Conclúyelo antes de activar otro.";
        }
        if (detail.contains("active_singleton_role") || detail.contains("uq_user_active_singleton")) {
            if (detail.contains("coordinator")) {
                return "Ya existe un coordinador activo. Inactívalo antes de activar o registrar a otro.";
            }
            return "Ya existe un administrador activo. Inactívalo antes de registrar a otro.";
        }
        if (detail.contains("email")) {
            return "Este correo ya está registrado. Use uno diferente.";
        }
        if (detail.contains("username")) {
            return "Este número de personal o usuario ya está en uso. Use otro.";
        }
        if (detail.contains("section") || detail.contains("uq_group_period_section")) {
            return "Ya existe un grupo con esa sección (NRC) en el periodo seleccionado.";
        }
        if (detail.contains("enrollment") || detail.contains("uq_enrollment_practitioner_period")) {
            return "El practicante ya está inscrito en un grupo de este periodo.";
        }
        if (detail.contains("uq_practitioner_document_type") || detail.contains("document_type")) {
            return "Ya subiste un documento de este tipo. Edítalo en lugar de subir otro.";
        }
        if (detail.contains("uq_grade_period")) {
            return "Este practicante ya tiene una calificación registrada para el periodo.";
        }
        if (detail.contains("uq_selfeval_report")) {
            return "Ya existe una autoevaluación registrada para este reporte.";
        }

        return GENERIC_DUPLICATE_MESSAGE;
    }
}
