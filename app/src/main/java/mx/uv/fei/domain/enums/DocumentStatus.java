package mx.uv.fei.domain.enums;

public enum DocumentStatus {

    PENDING("Pending", "Pendiente"),
    ACCEPTED("Accepted", "Aceptado"),
    REJECTED("Rejected", "Rechazado");

    private final String databaseValue;
    private final String displayName;

    DocumentStatus(String databaseValue, String displayName) {
        this.databaseValue = databaseValue;
        this.displayName = displayName;
    }

    public String getDatabaseValue() { return databaseValue; }
    public String getDisplayName() { return displayName; }

    public static DocumentStatus fromString(String databaseValue) {
        for (DocumentStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de documento no válido: " + databaseValue);
    }
}
