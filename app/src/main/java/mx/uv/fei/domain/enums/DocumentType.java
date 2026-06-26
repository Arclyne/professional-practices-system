package mx.uv.fei.domain.enums;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo de los tipos de documento que un practicante debe entregar.
 *
 * Cada tipo se identifica por un código estable (almacenado en la tabla {@code document_type})
 * y pertenece a una etapa ({@link DocumentCategory}). El nombre visible se muestra en la interfaz.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public enum DocumentType {

    CURP("CURP", "CURP", DocumentCategory.INITIAL),
    SCHEDULE("SCHEDULE", "Horario", DocumentCategory.INITIAL),
    INE("INE", "INE", DocumentCategory.INITIAL),
    ACCEPTANCE_LETTER("ACCEPTANCE_LETTER", "Carta de aceptación", DocumentCategory.INITIAL),
    RELEASE_LETTER("RELEASE_LETTER", "Carta de liberación", DocumentCategory.FINAL),
    ORGANIZATION_EVALUATION("ORGANIZATION_EVALUATION", "Evaluación de la organización", DocumentCategory.FINAL),
    PROFESSOR_EVALUATION("PROFESSOR_EVALUATION", "Evaluación del profesor", DocumentCategory.FINAL);

    private final String code;
    private final String displayName;
    private final DocumentCategory category;

    DocumentType(String code, String displayName, DocumentCategory category) {
        this.code = code;
        this.displayName = displayName;
        this.category = category;
    }

    public String getCode() { return code; }
    public String getDisplayName() { return displayName; }
    public DocumentCategory getCategory() { return category; }

    public static List<DocumentType> valuesForCategory(DocumentCategory category) {
        return Arrays.stream(values())
                .filter(type -> type.category == category)
                .toList();
    }

    public static DocumentType fromCode(String code) {
        for (DocumentType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de documento no válido: " + code);
    }
}
