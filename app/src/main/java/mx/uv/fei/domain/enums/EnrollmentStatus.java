package mx.uv.fei.domain.enums;

public enum EnrollmentStatus {

    ACTIVE("Active", "Activa"),
    INACTIVE("Inactive", "Inactiva");

    private final String databaseValue;
    private final String displayLabel;

    EnrollmentStatus(String databaseValue, String displayLabel) {
        this.databaseValue = databaseValue;
        this.displayLabel = displayLabel;
    }

    public String getDatabaseValue() { return databaseValue; }

    public String getDisplayLabel() { return displayLabel; }

    public static EnrollmentStatus fromString(String databaseValue) {
        for (EnrollmentStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de inscripción no válido: " + databaseValue);
    }
}
