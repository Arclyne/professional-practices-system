package mx.uv.fei.domain.enums;

public enum PeriodStatus {

    UPCOMING("Upcoming", "Próximo"),
    ACTIVE("Active", "Activo"),
    CONCLUDED("Concluded", "Concluido");

    private final String databaseValue;
    private final String displayLabel;

    PeriodStatus(String databaseValue, String displayLabel) {
        this.databaseValue = databaseValue;
        this.displayLabel = displayLabel;
    }

    public String getDatabaseValue() { return databaseValue; }

    public String getDisplayLabel() { return displayLabel; }

    public static PeriodStatus fromString(String databaseValue) {
        for (PeriodStatus status : values()) {
            if (status.databaseValue.equalsIgnoreCase(databaseValue)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Estado de periodo no válido: " + databaseValue);
    }
}
