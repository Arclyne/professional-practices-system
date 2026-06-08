package mx.uv.fei.domain.dto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DocumentTemplateTest {

    private DocumentTemplate templateA;
    private DocumentTemplate templateB;

    @BeforeEach
    void setUp() {
        templateA = new DocumentTemplate();
        templateA.setTemplateName("Oficio de Aceptación");
        templateA.setDocumentType("Oficio Oficial");
        templateA.setBodyContent("Cuerpo A con %NOMBRE_PRACTICANTE%.");
        templateA.setCreatedByUserName("coordinadora");

        templateB = new DocumentTemplate();
        templateB.setTemplateName("Oficio de Aceptación");
        templateB.setDocumentType("Oficio Oficial");
        templateB.setBodyContent("Cuerpo B completamente distinto.");
        templateB.setCreatedByUserName("otro_usuario");
    }



    @Test
    void constructor_Default_GeneratesNonNullId() {
        DocumentTemplate template = new DocumentTemplate();

        assertNotNull(template.getTemplateId());
    }

    @Test
    void constructor_Default_SetsCreatedAtToToday() {
        DocumentTemplate template = new DocumentTemplate();

        assertNotNull(template.getCreatedAt());
    }

    @Test
    void constructor_TwoCalls_GenerateDifferentIds() {
        DocumentTemplate first = new DocumentTemplate();
        DocumentTemplate second = new DocumentTemplate();

        assertNotEquals(first.getTemplateId(), second.getTemplateId());
    }



    @Test
    void equals_SameNameAndType_ReturnsTrue() {
        boolean result = templateA.equals(templateB);

        assertTrue(result);
    }

    @Test
    void equals_SameInstance_ReturnsTrue() {
        boolean result = templateA.equals(templateA);

        assertTrue(result);
    }

    @Test
    void equals_SameNameAndType_DifferentBody_ReturnsTrue() {
        templateB.setBodyContent("Contenido completamente diferente.");

        boolean result = templateA.equals(templateB);

        assertTrue(result);
    }

    @Test
    void equals_SameNameAndType_DifferentCreator_ReturnsTrue() {
        templateB.setCreatedByUserName("otro_coordinador");

        boolean result = templateA.equals(templateB);

        assertTrue(result);
    }


    @Test
    void equals_DifferentName_ReturnsFalse() {
        templateB.setTemplateName("Bitácora Mayo 2026");

        boolean result = templateA.equals(templateB);

        assertFalse(result);
    }

    @Test
    void equals_DifferentType_ReturnsFalse() {
        templateB.setDocumentType("Bitácora de Reporte Mensual");

        boolean result = templateA.equals(templateB);

        assertFalse(result);
    }

    @Test
    void equals_NullName_ReturnsFalse() {
        templateB.setTemplateName(null);

        boolean result = templateA.equals(templateB);

        assertFalse(result);
    }

    @Test
    void equals_ComparedToNull_ReturnsFalse() {
        boolean result = templateA.equals(null);

        assertFalse(result);
    }

    @Test
    void equals_ComparedToDifferentClass_ReturnsFalse() {
        boolean result = templateA.equals("Oficio de Aceptación");

        assertFalse(result);
    }

    @Test
    void hashCode_SameNameAndType_ProducesSameHash() {
        int hashA = templateA.hashCode();
        int hashB = templateB.hashCode();

        assertEquals(hashA, hashB);
    }

    @Test
    void hashCode_DifferentName_ProducesDifferentHash() {
        templateB.setTemplateName("Bitácora Mayo 2026");

        int hashA = templateA.hashCode();
        int hashB = templateB.hashCode();

        assertNotEquals(hashA, hashB);
    }

    @Test
    void toString_ContainsTemplateName() {
        String result = templateA.toString();

        assertTrue(result.contains("Oficio de Aceptación"));
    }

    @Test
    void toString_ContainsDocumentType() {
        String result = templateA.toString();

        assertTrue(result.contains("Oficio Oficial"));
    }
}
