package mx.uv.fei.domain.manager.reporting;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.DocumentTemplate;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

@Component
public class TemplateManager {

    private static final Logger log = LoggerFactory.getLogger(TemplateManager.class);

    private static final String TEMPLATES_SUBDIRECTORY = "templates";
    private static final String GENERATED_SUBDIRECTORY = "generated";
    private static final String DOCUMENTS_SUBDIRECTORY = "documents";
    private static final String METADATA_FILE_EXTENSION = ".meta";
    private static final String BODY_FILE_EXTENSION = ".body";
    private static final String OUTPUT_FILE_EXTENSION = ".txt";
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String FILE_ENCODING = "UTF-8";
    private static final String FILENAME_SANITIZE_REGEX = "[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ._-]";
    private static final String FILENAME_REPLACEMENT = "_";

    private static final String METADATA_KEY_NAME = "name";
    private static final String METADATA_KEY_TYPE = "type";
    private static final String METADATA_KEY_CREATED_BY = "createdBy";
    private static final String METADATA_KEY_CREATED_AT = "createdAt";

    private final String baseDirectory;

    public TemplateManager() {
        this.baseDirectory = "app";
    }

    TemplateManager(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public void saveTemplate(DocumentTemplate template) throws ManagerException {
        try {
            Path templateDirectory = resolveTemplateDirectory();
            saveMetadataFile(template, templateDirectory);
            saveBodyFile(template, templateDirectory);
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo guardar la plantilla en disco.", e);
        }
    }

    public List<DocumentTemplate> loadAllTemplates() throws ManagerException {
        List<DocumentTemplate> loadedTemplates = new ArrayList<>();
        try {
            Path templateDirectory = resolveTemplateDirectory();
            File[] metadataFiles = templateDirectory.toFile().listFiles(
                    file -> file.isFile() && file.getName().endsWith(METADATA_FILE_EXTENSION));

            if (metadataFiles != null) {
                for (File metadataFile : metadataFiles) {
                    DocumentTemplate recoveredTemplate = loadSingleTemplate(metadataFile, templateDirectory);
                    if (recoveredTemplate != null) {
                        loadedTemplates.add(recoveredTemplate);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al cargar las plantillas guardadas.", e);
        }
        return loadedTemplates;
    }

    public String renderTemplate(DocumentTemplate template, Map<String, String> tokenValues) {
        String renderedContent = template.getBodyContent();
        for (Map.Entry<String, String> tokenEntry : tokenValues.entrySet()) {
            String replacementValue = tokenEntry.getValue() != null ? tokenEntry.getValue() : "";
            renderedContent = renderedContent.replace(tokenEntry.getKey(), replacementValue);
        }
        return renderedContent;
    }

    public List<String> generateBatchDocuments(DocumentTemplate template, List<Map<String, String>> batchTokenValues) throws ManagerException {
        List<String> generatedFilePaths = new ArrayList<>();
        try {
            Path outputDirectory = resolveGeneratedDirectory();
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));
            int documentIndex = 1;

            for (Map<String, String> tokenValues : batchTokenValues) {
                String renderedContent = renderTemplate(template, tokenValues);
                String outputFileName = buildOutputFileName(currentDate, template.getTemplateName(), documentIndex);
                Path outputFilePath = outputDirectory.resolve(outputFileName);

                try (FileWriter writer = new FileWriter(outputFilePath.toFile(), StandardCharsets.UTF_8)) {
                    writer.write(renderedContent);
                }

                generatedFilePaths.add(outputFilePath.toAbsolutePath().toString());
                documentIndex++;
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al generar el documento desde la plantilla.", e);
        }
        return generatedFilePaths;
    }

    public void deleteTemplate(String templateId) throws ManagerException {
        try {
            Path templateDirectory = resolveTemplateDirectory();
            Files.deleteIfExists(templateDirectory.resolve(templateId + METADATA_FILE_EXTENSION));
            Files.deleteIfExists(templateDirectory.resolve(templateId + BODY_FILE_EXTENSION));
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo eliminar la plantilla.", e);
        }
    }

    private void saveMetadataFile(DocumentTemplate template, Path directory) throws IOException {
        Properties metadata = new Properties();
        metadata.setProperty(METADATA_KEY_NAME, template.getTemplateName());
        metadata.setProperty(METADATA_KEY_TYPE, template.getDocumentType());
        metadata.setProperty(METADATA_KEY_CREATED_BY,
                template.getCreatedByUserName() != null ? template.getCreatedByUserName() : "");
        metadata.setProperty(METADATA_KEY_CREATED_AT,
                template.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        Path metadataPath = directory.resolve(template.getTemplateId() + METADATA_FILE_EXTENSION);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(metadataPath.toFile()), FILE_ENCODING)) {
            metadata.store(writer, "DocumentTemplate Metadata");
        }
    }

    private void saveBodyFile(DocumentTemplate template, Path directory) throws IOException {
        Path bodyPath = directory.resolve(template.getTemplateId() + BODY_FILE_EXTENSION);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(bodyPath.toFile()), FILE_ENCODING)) {
            writer.write(template.getBodyContent() != null ? template.getBodyContent() : "");
        }
    }

    private DocumentTemplate loadSingleTemplate(File metadataFile, Path directory) throws IOException {
        DocumentTemplate recoveredTemplate = null;
        Properties metadata = new Properties();

        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(metadataFile), FILE_ENCODING)) {
            metadata.load(reader);
        }

        String templateId = metadataFile.getName().replace(METADATA_FILE_EXTENSION, "");
        Path bodyPath = directory.resolve(templateId + BODY_FILE_EXTENSION);

        if (bodyPath.toFile().exists()) {
            String bodyContent = Files.readString(bodyPath, StandardCharsets.UTF_8);
            recoveredTemplate = new DocumentTemplate();
            recoveredTemplate.setTemplateId(templateId);
            recoveredTemplate.setTemplateName(metadata.getProperty(METADATA_KEY_NAME, ""));
            recoveredTemplate.setDocumentType(metadata.getProperty(METADATA_KEY_TYPE, ""));
            recoveredTemplate.setCreatedByUserName(metadata.getProperty(METADATA_KEY_CREATED_BY, ""));
            recoveredTemplate.setBodyContent(bodyContent);

            String rawDate = metadata.getProperty(METADATA_KEY_CREATED_AT, "");
            if (!rawDate.isEmpty()) {
                recoveredTemplate.setCreatedAt(LocalDate.parse(rawDate, DateTimeFormatter.ofPattern(DATE_FORMAT)));
            }
        }

        return recoveredTemplate;
    }

    private Path resolveTemplateDirectory() throws IOException {
        return resolveDirectory(TEMPLATES_SUBDIRECTORY);
    }

    private Path resolveGeneratedDirectory() throws IOException {
        return resolveDirectory(GENERATED_SUBDIRECTORY);
    }

    private Path resolveDirectory(String subdirectory) throws IOException {
        Path directory = Paths.get(baseDirectory, DOCUMENTS_SUBDIRECTORY, subdirectory);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        return directory;
    }

    private String buildOutputFileName(String date, String templateName, int documentIndex) {
        return date + "_" + sanitizeForFileName(templateName) + "_doc" + documentIndex + OUTPUT_FILE_EXTENSION;
    }

    private String sanitizeForFileName(String name) {
        return name.replaceAll(FILENAME_SANITIZE_REGEX, FILENAME_REPLACEMENT);
    }
}