package mx.uv.fei.domain.enums;

public enum UserStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    PENDING("Pending");

    private final String databaseValue;

    UserStatus(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public static UserStatus fromString(String text) {
        UserStatus matchedStatus = null;

        for (UserStatus status : UserStatus.values()) {
            if (status.databaseValue.equalsIgnoreCase(text) && matchedStatus == null) {
                matchedStatus = status;
            }
        }

        if (matchedStatus == null) {
            throw new IllegalArgumentException("Estado de usuario no valido: " + text);
        }

        return matchedStatus;
    }
}
