package mx.uv.fei.domain.enums;


public enum ReportStatus {

    PENDING("Pendiente de Firma"),
    SUBMITTED("Entregado"),
    EVALUATED("Evaluado"),
    REJECTED("Rechazado");

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