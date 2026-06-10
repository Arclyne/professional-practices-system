package mx.uv.fei.domain.enums;

/**
 * Define los estados posibles de un usuario dentro del sistema.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public enum UserStatus {

    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING("Pending");

    private final String databaseValue;

    UserStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() { return databaseValue; }

    public static UserStatus fromString(String databaseValue) {
        for (UserStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de usuario no válido: " + databaseValue);
    }
}