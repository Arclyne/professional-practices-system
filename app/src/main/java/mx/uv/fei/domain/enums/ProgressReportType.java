package mx.uv.fei.domain.enums;

/**
 * Define los tipos de reporte de avance y sus horas requeridas de práctica.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
public enum ProgressReportType {

    INTERMEDIO("Intermedio", 210),
    FINAL("Final", 420);

    private final String databaseValue;
    private final int requiredHours;

    ProgressReportType(String databaseValue, int requiredHours) {
        this.databaseValue = databaseValue;
        this.requiredHours = requiredHours;
    }

    public String getDatabaseValue() { return databaseValue; }
    public int getRequiredHours() { return requiredHours; }

    public static ProgressReportType fromString(String databaseValue) {
        for (ProgressReportType type : values()) {
            if (type.databaseValue.equalsIgnoreCase(databaseValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de reporte no válido: " + databaseValue);
    }
}