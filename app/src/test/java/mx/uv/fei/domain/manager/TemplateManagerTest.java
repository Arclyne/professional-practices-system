package mx.uv.fei.domain.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.uv.fei.domain.dto.DocumentTemplate;
import mx.uv.fei.domain.exceptions.ManagerException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TemplateManagerTest {

    private static final String TEMPLATE_NAME    = "Oficio de Aceptación";
    private static final String TEMPLATE_TYPE    = "Oficio Oficial";
    private static final String TEMPLATE_BODY    = "Estimado %NOMBRE_PRACTICANTE%, matrícula %MATRICULA%.";
    private static final String CREATOR          = "coordinadora";

    @TempDir
    Path temporaryDirectory;

    private TemplateManager templateManager;
    private DocumentTemplate testTemplate;

    @BeforeEach
    void setUp() {
        templateManager = new TemplateManager(temporaryDirectory.toString());

        testTemplate = new DocumentTemplate();
        testTemplate.setTemplateName(TEMPLATE_NAME);
        testTemplate.setDocumentType(TEMPLATE_TYPE);
        testTemplate.setBodyContent(TEMPLATE_BODY);
        testTemplate.setCreatedByUserName(CREATOR);
    }


    @Test
    void saveTemplate_ValidTemplate_CreatesMetaFile() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        Path metaFile = temporaryDirectory.resolve(
                "documents/templates/" + testTemplate.getTemplateId() + ".meta");

        assertTrue(metaFile.toFile().exists());
    }

    @Test
    void saveTemplate_ValidTemplate_CreatesBodyFile() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        Path bodyFile = temporaryDirectory.resolve(
                "documents/templates/" + testTemplate.getTemplateId() + ".body");

        assertTrue(bodyFile.toFile().exists());
    }

    @Test
    void saveTemplate_ValidTemplate_CreatesTemplatesDirectory() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        Path dir = temporaryDirectory.resolve("documents/templates");

        assertTrue(dir.toFile().isDirectory());
    }

    @Test
    void saveTemplate_SameIdTwice_BodyFileContainsLatestContent() throws ManagerException, IOException {
        templateManager.saveTemplate(testTemplate);
        testTemplate.setBodyContent("Contenido actualizado.");

        templateManager.saveTemplate(testTemplate);

        Path bodyFile = temporaryDirectory.resolve(
                "documents/templates/" + testTemplate.getTemplateId() + ".body");
        String savedContent = Files.readString(bodyFile);

        assertEquals("Contenido actualizado.", savedContent);
    }

    @Test
    void loadAllTemplates_EmptyDirectory_ReturnsEmptyList() throws ManagerException {
        List<DocumentTemplate> result = templateManager.loadAllTemplates();

        assertTrue(result.isEmpty());
    }

    @Test
    void loadAllTemplates_AfterSavingOne_ReturnsOneElement() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        List<DocumentTemplate> result = templateManager.loadAllTemplates();

        assertEquals(1, result.size());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesTemplateName() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        DocumentTemplate recovered = templateManager.loadAllTemplates().getFirst();

        assertEquals(TEMPLATE_NAME, recovered.getTemplateName());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesDocumentType() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        DocumentTemplate recovered = templateManager.loadAllTemplates().getFirst();

        assertEquals(TEMPLATE_TYPE, recovered.getDocumentType());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesBodyContent() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        DocumentTemplate recovered = templateManager.loadAllTemplates().getFirst();

        assertEquals(TEMPLATE_BODY, recovered.getBodyContent());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesCreatedByUserName() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        DocumentTemplate recovered = templateManager.loadAllTemplates().getFirst();

        assertEquals(CREATOR, recovered.getCreatedByUserName());
    }

    @Test
    void loadAllTemplates_AfterSavingTwo_ReturnsBothTemplates() throws ManagerException {
        DocumentTemplate second = new DocumentTemplate();
        second.setTemplateName("Bitácora Mayo");
        second.setDocumentType("Bitácora de Reporte Mensual");
        second.setBodyContent("Actividades de %NOMBRE_PRACTICANTE%.");
        second.setCreatedByUserName(CREATOR);

        templateManager.saveTemplate(testTemplate);
        templateManager.saveTemplate(second);

        List<DocumentTemplate> result = templateManager.loadAllTemplates();

        assertEquals(2, result.size());
    }


    @Test
    void renderTemplate_WithMatchingTokens_ReplacesNombrePracticante() {
        Map<String, String> values = Map.of(
                "%NOMBRE_PRACTICANTE%", "Ángel Aguilar",
                "%MATRICULA%", "zS24242424"
        );

        String result = templateManager.renderTemplate(testTemplate, values);

        assertTrue(result.contains("Ángel Aguilar"));
    }

    @Test
    void renderTemplate_WithMatchingTokens_ReplacesMatricula() {
        Map<String, String> values = Map.of(
                "%NOMBRE_PRACTICANTE%", "Ángel Aguilar",
                "%MATRICULA%", "zS24242424"
        );

        String result = templateManager.renderTemplate(testTemplate, values);

        assertTrue(result.contains("zS24242424"));
    }

    @Test
    void renderTemplate_WithMatchingTokens_RemovesPlaceholder() {
        Map<String, String> values = Map.of(
                "%NOMBRE_PRACTICANTE%", "Ángel Aguilar",
                "%MATRICULA%", "zS24242424"
        );

        String result = templateManager.renderTemplate(testTemplate, values);

        assertFalse(result.contains("%NOMBRE_PRACTICANTE%"));
    }


    @Test
    void renderTemplate_NoMatchingToken_LeavesBodyUnchanged() {
        DocumentTemplate plainTemplate = new DocumentTemplate();
        plainTemplate.setTemplateName("Sin tokens");
        plainTemplate.setDocumentType(TEMPLATE_TYPE);
        plainTemplate.setBodyContent("Texto sin tokens de sustitución.");

        String result = templateManager.renderTemplate(plainTemplate, Map.of("%MATRICULA%", "zS001"));

        assertEquals("Texto sin tokens de sustitución.", result);
    }

    @Test
    void renderTemplate_NullTokenValue_SubstitutesEmptyString() {
        Map<String, String> values = new HashMap<>();
        values.put("%NOMBRE_PRACTICANTE%", null);
        values.put("%MATRICULA%", "zS24242424");

        String result = templateManager.renderTemplate(testTemplate, values);

        assertFalse(result.contains("%NOMBRE_PRACTICANTE%"));
    }

    @Test
    void renderTemplate_EmptyTokenMap_ReturnsOriginalBody() {
        String result = templateManager.renderTemplate(testTemplate, Map.of());

        assertEquals(TEMPLATE_BODY, result);
    }

    @Test
    void generateBatchDocuments_TwoEntries_ReturnsTwoPaths() throws ManagerException {
        List<Map<String, String>> batch = List.of(
                Map.of("%NOMBRE_PRACTICANTE%", "Ángel Aguilar", "%MATRICULA%", "zS001"),
                Map.of("%NOMBRE_PRACTICANTE%", "María López",   "%MATRICULA%", "zS002")
        );

        List<String> generatedPaths = templateManager.generateBatchDocuments(testTemplate, batch);

        assertEquals(2, generatedPaths.size());
    }

    @Test
    void generateBatchDocuments_OneEntry_CreatesFileOnDisk() throws ManagerException {
        List<Map<String, String>> batch = List.of(
                Map.of("%NOMBRE_PRACTICANTE%", "Ángel Aguilar", "%MATRICULA%", "zS24242424")
        );

        List<String> generatedPaths = templateManager.generateBatchDocuments(testTemplate, batch);

        assertTrue(Path.of(generatedPaths.getFirst()).toFile().exists());
    }

    @Test
    void generateBatchDocuments_OneEntry_FileContainsSubstitutedName() throws ManagerException, IOException {
        List<Map<String, String>> batch = List.of(
                Map.of("%NOMBRE_PRACTICANTE%", "Ángel Aguilar", "%MATRICULA%", "zS24242424")
        );

        List<String> generatedPaths = templateManager.generateBatchDocuments(testTemplate, batch);
        String content = Files.readString(Path.of(generatedPaths.getFirst()));

        assertTrue(content.contains("Ángel Aguilar"));
    }

    @Test
    void generateBatchDocuments_EmptyBatch_ReturnsEmptyList() throws ManagerException {
        List<String> generatedPaths = templateManager.generateBatchDocuments(testTemplate, List.of());

        assertTrue(generatedPaths.isEmpty());
    }

    @Test
    void deleteTemplate_ExistingTemplate_RemovesMetaFile() throws ManagerException {
        templateManager.saveTemplate(testTemplate);
        Path metaFile = temporaryDirectory.resolve(
                "documents/templates/" + testTemplate.getTemplateId() + ".meta");

        templateManager.deleteTemplate(testTemplate.getTemplateId());

        assertFalse(metaFile.toFile().exists());
    }

    @Test
    void deleteTemplate_ExistingTemplate_RemovesBodyFile() throws ManagerException {
        templateManager.saveTemplate(testTemplate);
        Path bodyFile = temporaryDirectory.resolve(
                "documents/templates/" + testTemplate.getTemplateId() + ".body");

        templateManager.deleteTemplate(testTemplate.getTemplateId());

        assertFalse(bodyFile.toFile().exists());
    }

    @Test
    void deleteTemplate_AfterDelete_NotReturnedByLoadAll() throws ManagerException {
        templateManager.saveTemplate(testTemplate);

        templateManager.deleteTemplate(testTemplate.getTemplateId());

        List<DocumentTemplate> result = templateManager.loadAllTemplates();
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteTemplate_NonExistentId_DoesNotThrowException() {
        assertDoesNotThrow(()-> templateManager.deleteTemplate("uuid-que-no-existe"));
    }
}
