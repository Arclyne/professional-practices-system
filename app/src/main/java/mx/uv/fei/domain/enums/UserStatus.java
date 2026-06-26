package mx.uv.fei.domain.enums;


public enum UserStatus {

    ACTIVE("Active", "Activo"),
    INACTIVE("Inactive", "Inactivo"),
    PENDING("Pending", "Pendiente");

    private final String databaseValue;
    private final String displayLabel;

    UserStatus(String databaseValue, String displayLabel) {
        this.databaseValue = databaseValue;
        this.displayLabel = displayLabel;
    }

    public String getDatabaseValue() { return databaseValue; }

    public String getDisplayLabel() { return displayLabel; }

    public static UserStatus fromString(String databaseValue) {
        for (UserStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de usuario no válido: " + databaseValue);
    }
}