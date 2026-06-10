package mx.uv.fei.domain.enums;

/**
 * Define los estados posibles de un reporte dentro del flujo de evaluación.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public enum ReportStatus {

    PENDING("Pendiente de Firma"),
    SUBMITTED("Entregado"),
    EVALUATED("Evaluado");

    private final String databaseValue;

    ReportStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() { return databaseValue; }

    public static ReportStatus fromString(String databaseValue) {
        for (ReportStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de reporte no válido: " + databaseValue);
    }
}