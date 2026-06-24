package mx.uv.fei.domain.enums;

/**
 * Distingue la etapa a la que pertenece un documento del expediente del practicante.
 *
 * Los documentos iniciales habilitan el arranque de prácticas, mientras que los finales
 * (carta de liberación y similares) corresponden al cierre del proceso.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public enum DocumentType {

    INITIAL("Inicial"),
    FINAL("Final");

    private final String databaseValue;

    DocumentType(String databaseValue) {
        this.databaseValue = databaseValue;
    }

    public String getDatabaseValue() { return databaseValue; }

    public static DocumentType fromString(String databaseValue) {
        for (DocumentType type : values()) {
            if (type.databaseValue.equalsIgnoreCase(databaseValue)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de documento no válido: " + databaseValue);
    }
}
