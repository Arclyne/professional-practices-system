package mx.uv.fei.domain.enums;

public enum Gender {
    MALE("Male", "Masculino"),
    FEMALE("Female", "Femenino"),
    OTHER("Other", "Otro");

    private final String databaseValue;
    private final String displayValue;

    Gender(String databaseValue, String displayValue) {
        this.databaseValue = databaseValue;
        this.displayValue = displayValue;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public String getDisplayValue() {
        return displayValue;
    }

    public static Gender fromDatabaseValue(String text) {
        for (Gender gender : Gender.values()) {
            if (gender.databaseValue.equalsIgnoreCase(text)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Género en base de datos no válido: " + text);
    }

    public static Gender fromDisplayValue(String text) {
        for (Gender gender : Gender.values()) {
            if (gender.displayValue.equalsIgnoreCase(text)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Género de interfaz no válido: " + text);
    }
}