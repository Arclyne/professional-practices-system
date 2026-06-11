package mx.uv.fei.domain.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DocumentTemplateTest {

    private DocumentTemplate baseTemplate;
    private DocumentTemplate comparedTemplate;

    @BeforeEach
    void setUp() {
        baseTemplate = new DocumentTemplate();
        baseTemplate.setTemplateName("Oficio de Aceptacion");
        baseTemplate.setDocumentType("Oficio Oficial");
        baseTemplate.setBodyContent("Estimado %NOMBRE_PRACTICANTE%, su proyecto fue aceptado.");
        baseTemplate.setCreatedByUserName("mrodriguez");

        comparedTemplate = new DocumentTemplate();
        comparedTemplate.setTemplateName("Oficio de Aceptacion");
        comparedTemplate.setDocumentType("Oficio Oficial");
        comparedTemplate.setBodyContent("Cuerpo alternativo del oficio de aceptacion.");
        comparedTemplate.setCreatedByUserName("eprior");
    }

    @Test
    void constructor_Default_GeneratesNonNullId() {
        DocumentTemplate emptyTemplate = new DocumentTemplate();

        assertNotNull(emptyTemplate.getTemplateId());
    }

    @Test
    void constructor_Default_SetsCreatedAtToToday() {
        DocumentTemplate emptyTemplate = new DocumentTemplate();

        assertNotNull(emptyTemplate.getCreatedAt());
    }

    @Test
    void constructor_TwoCalls_GenerateDifferentIds() {
        DocumentTemplate firstTemplate = new DocumentTemplate();
        DocumentTemplate secondTemplate = new DocumentTemplate();

        assertNotEquals(firstTemplate.getTemplateId(), secondTemplate.getTemplateId());
    }

    @Test
    void equals_SameNameAndType_ReturnsTrue() {
        boolean isEqual = baseTemplate.equals(comparedTemplate);

        assertTrue(isEqual);
    }

    @Test
    void equals_SameInstance_ReturnsTrue() {
        boolean isEqual = baseTemplate.equals(baseTemplate);

        assertTrue(isEqual);
    }

    @Test
    void equals_SameNameAndType_DifferentBody_ReturnsTrue() {
        comparedTemplate.setBodyContent("Contenido completamente diferente.");

        boolean isEqual = baseTemplate.equals(comparedTemplate);

        assertTrue(isEqual);
    }

    @Test
    void equals_SameNameAndType_DifferentCreator_ReturnsTrue() {
        comparedTemplate.setCreatedByUserName("pluna");

        boolean isEqual = baseTemplate.equals(comparedTemplate);

        assertTrue(isEqual);
    }

    @Test
    void equals_DifferentName_ReturnsFalse() {
        comparedTemplate.setTemplateName("Bitacora Mayo 2026");

        boolean isEqual = baseTemplate.equals(comparedTemplate);

        assertFalse(isEqual);
    }

    @Test
    void equals_DifferentType_ReturnsFalse() {
        comparedTemplate.setDocumentType("Bitacora de Reporte Mensual");

        boolean isEqual = baseTemplate.equals(comparedTemplate);

        assertFalse(isEqual);
    }

    @Test
    void equals_NullName_ReturnsFalse() {
        comparedTemplate.setTemplateName(null);

        boolean isEqual = baseTemplate.equals(comparedTemplate);

        assertFalse(isEqual);
    }

    @Test
    void equals_ComparedToNull_ReturnsFalse() {
        boolean isEqual = baseTemplate.equals(null);

        assertFalse(isEqual);
    }

    @Test
    void equals_ComparedToDifferentClass_ReturnsFalse() {
        boolean isEqual = baseTemplate.equals("Oficio de Aceptacion");

        assertFalse(isEqual);
    }

    @Test
    void hashCode_SameNameAndType_ProducesSameHash() {
        int baseHash = baseTemplate.hashCode();
        int comparedHash = comparedTemplate.hashCode();

        assertEquals(baseHash, comparedHash);
    }

    @Test
    void hashCode_DifferentName_ProducesDifferentHash() {
        comparedTemplate.setTemplateName("Bitacora Mayo 2026");

        int baseHash = baseTemplate.hashCode();
        int comparedHash = comparedTemplate.hashCode();

        assertNotEquals(baseHash, comparedHash);
    }

    @Test
    void toString_ContainsTemplateName() {
        String templateDescription = baseTemplate.toString();

        assertTrue(templateDescription.contains("Oficio de Aceptacion"));
    }

    @Test
    void toString_ContainsDocumentType() {
        String templateDescription = baseTemplate.toString();

        assertTrue(templateDescription.contains("Oficio Oficial"));
    }
}
