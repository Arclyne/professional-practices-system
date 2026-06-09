package mx.uv.fei.domain.enums;

public enum ProgressReportType {

    INTERMEDIO("Intermedio", 210),
    FINAL("Final", 420);

    private final String databaseValue;
    private final int requiredHours;

    ProgressReportType(String databaseValue, int requiredHours) {
        this.databaseValue = databaseValue;
        this.requiredHours = requiredHours;
    }

    public String getDatabaseValue() {
        return databaseValue;
    }

    public int getRequiredHours() {
        return requiredHours;
    }

    public static ProgressReportType fromString(String text) {
        ProgressReportType matched = null;

        for (ProgressReportType type : ProgressReportType.values()) {
            if (type.databaseValue.equalsIgnoreCase(text)) {
                matched = type;
                break;
            }
        }

        if (matched == null) {
            throw new IllegalArgumentException("Tipo de reporte no válido: " + text);
        }

        return matched;
    }
}
