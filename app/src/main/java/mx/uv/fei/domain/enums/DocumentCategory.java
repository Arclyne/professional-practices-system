package mx.uv.fei.domain.enums;

/**
 * Distingue la etapa de prácticas a la que pertenece un tipo de documento.
 *
 * Los documentos iniciales habilitan el arranque de prácticas, mientras que los finales
 * (carta de liberación) corresponden al cierre del proceso.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
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
