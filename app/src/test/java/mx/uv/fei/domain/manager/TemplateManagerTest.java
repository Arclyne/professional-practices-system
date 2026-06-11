package mx.uv.fei.domain.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mx.uv.fei.domain.dto.DocumentTemplate;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class TemplateManagerTest {

    private static final String TEMPLATE_NAME = "Oficio de Aceptacion";
    private static final String TEMPLATE_TYPE = "Oficio Oficial";
    private static final String TEMPLATE_BODY = "Estimado %NOMBRE_PRACTICANTE%, matricula %MATRICULA%.";
    private static final String CREATOR_USERNAME = "mrodriguez";
    private static final String PRACTITIONER_FULL_NAME = "Angel Gabriel Aguilar Hernandez";
    private static final String PRACTITIONER_ENROLLMENT = "zS24242424";
    private static final String NAME_TOKEN = "%NOMBRE_PRACTICANTE%";
    private static final String ENROLLMENT_TOKEN = "%MATRICULA%";
    private static final String TEMPLATES_DIRECTORY = "documents/templates/";

    @TempDir
    Path temporaryDirectory;

    private TemplateManager templateManager;
    private DocumentTemplate acceptanceTemplate;

    @BeforeEach
    void setUp() {
        templateManager = new TemplateManager(temporaryDirectory.toString());

        acceptanceTemplate = new DocumentTemplate();
        acceptanceTemplate.setTemplateName(TEMPLATE_NAME);
        acceptanceTemplate.setDocumentType(TEMPLATE_TYPE);
        acceptanceTemplate.setBodyContent(TEMPLATE_BODY);
        acceptanceTemplate.setCreatedByUserName(CREATOR_USERNAME);
    }

    private Map<String, String> buildPractitionerTokenValues() {
        return Map.of(
                NAME_TOKEN, PRACTITIONER_FULL_NAME,
                ENROLLMENT_TOKEN, PRACTITIONER_ENROLLMENT);
    }

    @Test
    void saveTemplate_ValidTemplate_CreatesMetaFile() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        Path metaFile = temporaryDirectory.resolve(TEMPLATES_DIRECTORY + acceptanceTemplate.getTemplateId() + ".meta");

        assertTrue(metaFile.toFile().exists());
    }

    @Test
    void saveTemplate_ValidTemplate_CreatesBodyFile() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        Path bodyFile = temporaryDirectory.resolve(TEMPLATES_DIRECTORY + acceptanceTemplate.getTemplateId() + ".body");

        assertTrue(bodyFile.toFile().exists());
    }

    @Test
    void saveTemplate_ValidTemplate_CreatesTemplatesDirectory() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        Path templatesDirectory = temporaryDirectory.resolve("documents/templates");

        assertTrue(templatesDirectory.toFile().isDirectory());
    }

    @Test
    void saveTemplate_SameIdTwice_BodyFileContainsLatestContent() throws ManagerException, IOException {
        templateManager.saveTemplate(acceptanceTemplate);
        acceptanceTemplate.setBodyContent("Contenido actualizado.");

        templateManager.saveTemplate(acceptanceTemplate);

        Path bodyFile = temporaryDirectory.resolve(TEMPLATES_DIRECTORY + acceptanceTemplate.getTemplateId() + ".body");
        String savedContent = Files.readString(bodyFile);
        assertEquals("Contenido actualizado.", savedContent);
    }

    @Test
    void loadAllTemplates_EmptyDirectory_ReturnsEmptyList() throws ManagerException {
        List<DocumentTemplate> loadedTemplates = templateManager.loadAllTemplates();

        assertTrue(loadedTemplates.isEmpty());
    }

    @Test
    void loadAllTemplates_AfterSavingOne_ReturnsOneElement() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        List<DocumentTemplate> loadedTemplates = templateManager.loadAllTemplates();

        assertEquals(1, loadedTemplates.size());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesTemplateName() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        DocumentTemplate recoveredTemplate = templateManager.loadAllTemplates().getFirst();

        assertEquals(TEMPLATE_NAME, recoveredTemplate.getTemplateName());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesDocumentType() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        DocumentTemplate recoveredTemplate = templateManager.loadAllTemplates().getFirst();

        assertEquals(TEMPLATE_TYPE, recoveredTemplate.getDocumentType());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesBodyContent() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        DocumentTemplate recoveredTemplate = templateManager.loadAllTemplates().getFirst();

        assertEquals(TEMPLATE_BODY, recoveredTemplate.getBodyContent());
    }

    @Test
    void loadAllTemplates_AfterSaving_PreservesCreatedByUserName() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        DocumentTemplate recoveredTemplate = templateManager.loadAllTemplates().getFirst();

        assertEquals(CREATOR_USERNAME, recoveredTemplate.getCreatedByUserName());
    }

    @Test
    void loadAllTemplates_AfterSavingTwo_ReturnsBothTemplates() throws ManagerException {
        DocumentTemplate logbookTemplate = new DocumentTemplate();
        logbookTemplate.setTemplateName("Bitacora Mayo");
        logbookTemplate.setDocumentType("Bitacora de Reporte Mensual");
        logbookTemplate.setBodyContent("Actividades de %NOMBRE_PRACTICANTE%.");
        logbookTemplate.setCreatedByUserName(CREATOR_USERNAME);

        templateManager.saveTemplate(acceptanceTemplate);
        templateManager.saveTemplate(logbookTemplate);

        List<DocumentTemplate> loadedTemplates = templateManager.loadAllTemplates();
        assertEquals(2, loadedTemplates.size());
    }

    @Test
    void renderTemplate_WithMatchingTokens_ReplacesNombrePracticante() {
        Map<String, String> tokenValues = buildPractitionerTokenValues();

        String renderedBody = templateManager.renderTemplate(acceptanceTemplate, tokenValues);

        assertTrue(renderedBody.contains(PRACTITIONER_FULL_NAME));
    }

    @Test
    void renderTemplate_WithMatchingTokens_ReplacesMatricula() {
        Map<String, String> tokenValues = buildPractitionerTokenValues();

        String renderedBody = templateManager.renderTemplate(acceptanceTemplate, tokenValues);

        assertTrue(renderedBody.contains(PRACTITIONER_ENROLLMENT));
    }

    @Test
    void renderTemplate_WithMatchingTokens_RemovesPlaceholder() {
        Map<String, String> tokenValues = buildPractitionerTokenValues();

        String renderedBody = templateManager.renderTemplate(acceptanceTemplate, tokenValues);

        assertFalse(renderedBody.contains(NAME_TOKEN));
    }

    @Test
    void renderTemplate_NoMatchingToken_LeavesBodyUnchanged() {
        DocumentTemplate plainTemplate = new DocumentTemplate();
        plainTemplate.setTemplateName("Aviso general");
        plainTemplate.setDocumentType(TEMPLATE_TYPE);
        plainTemplate.setBodyContent("Texto sin tokens de sustitucion.");

        String renderedBody = templateManager.renderTemplate(plainTemplate,
                Map.of(ENROLLMENT_TOKEN, PRACTITIONER_ENROLLMENT));

        assertEquals("Texto sin tokens de sustitucion.", renderedBody);
    }

    @Test
    void renderTemplate_NullTokenValue_SubstitutesEmptyString() {
        Map<String, String> tokenValues = new HashMap<>();
        tokenValues.put(NAME_TOKEN, null);
        tokenValues.put(ENROLLMENT_TOKEN, PRACTITIONER_ENROLLMENT);

        String renderedBody = templateManager.renderTemplate(acceptanceTemplate, tokenValues);

        assertFalse(renderedBody.contains(NAME_TOKEN));
    }

    @Test
    void renderTemplate_EmptyTokenMap_ReturnsOriginalBody() {
        String renderedBody = templateManager.renderTemplate(acceptanceTemplate, Map.of());

        assertEquals(TEMPLATE_BODY, renderedBody);
    }

    @Test
    void generateBatchDocuments_TwoEntries_ReturnsTwoPaths() throws ManagerException {
        List<Map<String, String>> batchEntries = List.of(
                buildPractitionerTokenValues(),
                Map.of(NAME_TOKEN, "Daniela Morales Vazquez", ENROLLMENT_TOKEN, "zS23151617"));

        List<String> generatedPaths = templateManager.generateBatchDocuments(acceptanceTemplate, batchEntries);

        assertEquals(2, generatedPaths.size());
    }

    @Test
    void generateBatchDocuments_OneEntry_CreatesFileOnDisk() throws ManagerException {
        List<Map<String, String>> batchEntries = List.of(buildPractitionerTokenValues());

        List<String> generatedPaths = templateManager.generateBatchDocuments(acceptanceTemplate, batchEntries);

        assertTrue(Path.of(generatedPaths.getFirst()).toFile().exists());
    }

    @Test
    void generateBatchDocuments_OneEntry_FileContainsSubstitutedName() throws ManagerException, IOException {
        List<Map<String, String>> batchEntries = List.of(buildPractitionerTokenValues());

        List<String> generatedPaths = templateManager.generateBatchDocuments(acceptanceTemplate, batchEntries);

        String generatedContent = Files.readString(Path.of(generatedPaths.getFirst()));
        assertTrue(generatedContent.contains(PRACTITIONER_FULL_NAME));
    }

    @Test
    void generateBatchDocuments_EmptyBatch_ReturnsEmptyList() throws ManagerException {
        List<String> generatedPaths = templateManager.generateBatchDocuments(acceptanceTemplate, List.of());

        assertTrue(generatedPaths.isEmpty());
    }

    @Test
    void deleteTemplate_ExistingTemplate_RemovesMetaFile() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);
        Path metaFile = temporaryDirectory.resolve(TEMPLATES_DIRECTORY + acceptanceTemplate.getTemplateId() + ".meta");

        templateManager.deleteTemplate(acceptanceTemplate.getTemplateId());

        assertFalse(metaFile.toFile().exists());
    }

    @Test
    void deleteTemplate_ExistingTemplate_RemovesBodyFile() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);
        Path bodyFile = temporaryDirectory.resolve(TEMPLATES_DIRECTORY + acceptanceTemplate.getTemplateId() + ".body");

        templateManager.deleteTemplate(acceptanceTemplate.getTemplateId());

        assertFalse(bodyFile.toFile().exists());
    }

    @Test
    void deleteTemplate_AfterDelete_NotReturnedByLoadAll() throws ManagerException {
        templateManager.saveTemplate(acceptanceTemplate);

        templateManager.deleteTemplate(acceptanceTemplate.getTemplateId());

        List<DocumentTemplate> loadedTemplates = templateManager.loadAllTemplates();
        assertTrue(loadedTemplates.isEmpty());
    }

    @Test
    void deleteTemplate_NonExistentId_DoesNotThrowException() {
        assertDoesNotThrow(() -> templateManager.deleteTemplate("identificador-inexistente"));
    }
}
