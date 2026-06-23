package mx.uv.fei.domain.enums;

/**
 * Define los estados posibles de un documento subido por un practicante.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public enum DocumentStatus {

    PENDING("Pendiente"),
    REVIEWED("Revisado");

    private final String databaseValue;

    DocumentStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() { return databaseValue; }

    public static DocumentStatus fromString(String databaseValue) {
        for (DocumentStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de documento no válido: " + databaseValue);
    }
}
