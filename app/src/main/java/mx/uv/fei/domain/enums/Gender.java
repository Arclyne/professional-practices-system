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

    public String getDatabaseValue() { return databaseValue; }
    public String getDisplayValue() { return displayValue; }

    public static Gender fromDatabaseValue(String databaseValue) {
        for (Gender gender : values()) {
            if (gender.databaseValue.equalsIgnoreCase(databaseValue)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Género en base de datos no válido: " + databaseValue);
    }

    public static Gender fromDisplayValue(String displayValue) {
        for (Gender gender : values()) {
            if (gender.displayValue.equalsIgnoreCase(displayValue)) {
                return gender;
            }
        }
        throw new IllegalArgumentException("Género de interfaz no válido: " + displayValue);
    }
}