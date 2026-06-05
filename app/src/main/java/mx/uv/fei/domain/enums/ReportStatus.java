package mx.uv.fei.domain.enums;

public enum ReportStatus {
    PENDING("Pendiente de Firma"),
    SUBMITTED("Entregado"),
    EVALUATED("Evaluado");

    private final String databaseValue;

    ReportStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public static ReportStatus fromString(String text) {
        for (ReportStatus status : ReportStatus.values()) {
            if (status.databaseValue.equalsIgnoreCase(text)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de reporte no válido: " + text);
    }
}