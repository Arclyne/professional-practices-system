package mx.uv.fei.domain.enums;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateTokenTest {


    @Test
    void getPlaceholder_NombrePracticante_StartsWithPercent() {
        String placeholder = TemplateToken.NOMBRE_PRACTICANTE.getPlaceholder();

        assertTrue(placeholder.startsWith("%"));
    }

    @Test
    void getPlaceholder_NombrePracticante_EndsWithPercent() {
        String placeholder = TemplateToken.NOMBRE_PRACTICANTE.getPlaceholder();

        assertTrue(placeholder.endsWith("%"));
    }

    @Test
    void getPlaceholder_FechaActual_HasCorrectValue() {
        String placeholder = TemplateToken.FECHA_ACTUAL.getPlaceholder();

        assertEquals("%FECHA_ACTUAL%", placeholder);
    }

    @Test
    void getPlaceholder_Matricula_HasCorrectValue() {
        String placeholder = TemplateToken.MATRICULA.getPlaceholder();

        assertEquals("%MATRICULA%", placeholder);
    }


    @Test
    void getDescription_NombrePracticante_IsNotNull() {
        String description = TemplateToken.NOMBRE_PRACTICANTE.getDescription();

        assertNotNull(description);
    }

    @Test
    void getDescription_NombrePracticante_IsNotBlank() {
        String description = TemplateToken.NOMBRE_PRACTICANTE.getDescription();

        assertFalse(description.isBlank());
    }

    @Test
    void toString_NombrePracticante_ContainsPlaceholder() {
        String displayText = TemplateToken.NOMBRE_PRACTICANTE.toString();

        assertTrue(displayText.contains("%NOMBRE_PRACTICANTE%"));
    }

    @Test
    void toString_NombrePracticante_ContainsDescription() {
        String displayText = TemplateToken.NOMBRE_PRACTICANTE.toString();

        assertTrue(displayText.contains(TemplateToken.NOMBRE_PRACTICANTE.getDescription()));
    }


    @Test
    void allTokens_HaveNonBlankPlaceholder() {
        boolean allHavePlaceholder = Arrays.stream(TemplateToken.values())
                .allMatch(token -> token.getPlaceholder() != null && !token.getPlaceholder().isBlank());

        assertTrue(allHavePlaceholder);
    }

    @Test
    void allTokens_HaveNonBlankDescription() {
        boolean allHaveDescription = Arrays.stream(TemplateToken.values())
                .allMatch(token -> token.getDescription() != null && !token.getDescription().isBlank());

        assertTrue(allHaveDescription);
    }

    @Test
    void allTokens_PlaceholdersStartWithPercent() {
        boolean allStart = Arrays.stream(TemplateToken.values())
                .allMatch(token -> token.getPlaceholder().startsWith("%"));

        assertTrue(allStart);
    }

    @Test
    void allTokens_PlaceholdersEndWithPercent() {
        boolean allEnd = Arrays.stream(TemplateToken.values())
                .allMatch(token -> token.getPlaceholder().endsWith("%"));

        assertTrue(allEnd);
    }



    @Test
    void allTokens_PlaceholdersAreUnique() {
        List<String> placeholders = Arrays.stream(TemplateToken.values())
                .map(TemplateToken::getPlaceholder)
                .toList();
        long distinctCount = placeholders.stream().distinct().count();

        assertEquals(TemplateToken.values().length, distinctCount);
    }


    @Test
    void allTokens_CountIsTen() {
        int tokenCount = TemplateToken.values().length;

        assertEquals(10, tokenCount);
    }
}
