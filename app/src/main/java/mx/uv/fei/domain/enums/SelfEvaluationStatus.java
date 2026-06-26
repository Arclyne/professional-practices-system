package mx.uv.fei.domain.enums;


/**
 * Define los estados posibles de una autoevaluación dentro del flujo de revisión.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public enum SelfEvaluationStatus {

    PENDING("Pendiente"),
    REVIEWED("Revisada"),
    REJECTED("Rechazada");

    private final String databaseValue;

    SelfEvaluationStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() { return databaseValue; }

    public static SelfEvaluationStatus fromString(String databaseValue) {
        for (SelfEvaluationStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de autoevaluación no válido: " + databaseValue);
    }
}
