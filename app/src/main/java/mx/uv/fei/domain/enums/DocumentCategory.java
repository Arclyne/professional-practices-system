package mx.uv.fei.domain.enums;

public enum DocumentCategory {

    INITIAL("Initial"),
    FINAL("Final");

    private final String databaseValue;

    DocumentCategory(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() { return databaseValue; }

    public static DocumentCategory fromDatabaseValue(String databaseValue) {
        for (DocumentCategory category : values()) {
            if (category.databaseValue.equalsIgnoreCase(databaseValue)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Categoría de documento no válida: " + databaseValue);
    }
}
