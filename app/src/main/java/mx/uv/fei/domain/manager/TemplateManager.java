package mx.uv.fei.domain.manager;

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

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.DocumentTemplate;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class TemplateManager {

    private static final String DIR_APP        = "app";
    private static final String DIR_DOCUMENTS  = "documents";
    private static final String DIR_TEMPLATES  = "templates";
    private static final String DIR_GENERATED  = "generated";

    private final String baseDirectory;
    private static final String EXT_META       = ".meta";
    private static final String EXT_BODY       = ".body";
    private static final String EXT_OUTPUT     = ".txt";
    private static final String DATE_FORMAT    = "dd-MM-yyyy";
    private static final String CHARSET        = "UTF-8";

    private static final String META_NAME      = "name";
    private static final String META_TYPE      = "type";
    private static final String META_CREATED_BY= "createdBy";
    private static final String META_CREATED_AT= "createdAt";

    private static final String MSG_SAVE_ERROR = "No se pudo guardar la plantilla en disco.";
    private static final String MSG_LOAD_ERROR = "Error al cargar las plantillas guardadas.";
    private static final String MSG_DELETE_ERROR = "No se pudo eliminar la plantilla.";
    private static final String MSG_GENERATE_ERROR = "Error al generar el documento desde la plantilla.";


    public TemplateManager() {
        this.baseDirectory = DIR_APP;
    }


    TemplateManager(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }


    public void saveTemplate(DocumentTemplate template) throws ManagerException {
        try {
            Path templateDirectory = resolveTemplateDirectory();

            saveMetadataFile(template, templateDirectory);
            saveBodyFile(template, templateDirectory);

        } catch (IOException exception) {
            throw new ManagerException(MSG_SAVE_ERROR + " Causa: " + exception.getMessage(), exception);
        }
    }

    private void saveMetadataFile(DocumentTemplate template, Path directory) throws IOException {
        Properties metadata = new Properties();
        metadata.setProperty(META_NAME, template.getTemplateName());
        metadata.setProperty(META_TYPE, template.getDocumentType());
        metadata.setProperty(META_CREATED_BY, template.getCreatedByUserName() != null ? template.getCreatedByUserName() : "");
        metadata.setProperty(META_CREATED_AT, template.getCreatedAt().format(DateTimeFormatter.ofPattern(DATE_FORMAT)));

        Path metaPath = directory.resolve(template.getTemplateId() + EXT_META);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(metaPath.toFile()), CHARSET)) {
            metadata.store(writer, "DocumentTemplate Metadata");
        }
    }

    private void saveBodyFile(DocumentTemplate template, Path directory) throws IOException {
        Path bodyPath = directory.resolve(template.getTemplateId() + EXT_BODY);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(bodyPath.toFile()), CHARSET)) {
            writer.write(template.getBodyContent() != null ? template.getBodyContent() : "");
        }
    }


    public List<DocumentTemplate> loadAllTemplates() throws ManagerException {
        List<DocumentTemplate> loadedTemplates = new ArrayList<>();

        try {
            Path templateDirectory = resolveTemplateDirectory();
            File[] metaFiles = templateDirectory.toFile().listFiles(
                    file -> file.isFile() && file.getName().endsWith(EXT_META)
            );

            if (metaFiles != null) {
                for (File metaFile : metaFiles) {
                    DocumentTemplate recoveredTemplate = loadSingleTemplate(metaFile, templateDirectory);
                    if (recoveredTemplate != null) {
                        loadedTemplates.add(recoveredTemplate);
                    }
                }
            }
        } catch (IOException exception) {
            throw new ManagerException(MSG_LOAD_ERROR + " Causa: " + exception.getMessage(), exception);
        }

        return loadedTemplates;
    }

    private DocumentTemplate loadSingleTemplate(File metaFile, Path directory) throws IOException {
        DocumentTemplate recoveredTemplate = null;

        Properties metadata = new Properties();
        try (InputStreamReader reader = new InputStreamReader(new FileInputStream(metaFile), CHARSET)) {
            metadata.load(reader);
        }

        String templateId = metaFile.getName().replace(EXT_META, "");
        Path bodyPath = directory.resolve(templateId + EXT_BODY);

        if (bodyPath.toFile().exists()) {
            String bodyContent = Files.readString(bodyPath, StandardCharsets.UTF_8);

            recoveredTemplate = new DocumentTemplate();
            recoveredTemplate.setTemplateId(templateId);
            recoveredTemplate.setTemplateName(metadata.getProperty(META_NAME, ""));
            recoveredTemplate.setDocumentType(metadata.getProperty(META_TYPE, ""));
            recoveredTemplate.setCreatedByUserName(metadata.getProperty(META_CREATED_BY, ""));
            recoveredTemplate.setBodyContent(bodyContent);

            String rawDate = metadata.getProperty(META_CREATED_AT, "");
            if (!rawDate.isEmpty()) {
                recoveredTemplate.setCreatedAt(LocalDate.parse(rawDate, DateTimeFormatter.ofPattern(DATE_FORMAT)));
            }
        }

        return recoveredTemplate;
    }


    public String renderTemplate(DocumentTemplate template, Map<String, String> tokenValues) {
        String renderedContent = template.getBodyContent();

        for (Map.Entry<String, String> entry : tokenValues.entrySet()) {
            String replacementValue = entry.getValue() != null ? entry.getValue() : "";
            renderedContent = renderedContent.replace(entry.getKey(), replacementValue);
        }

        return renderedContent;
    }


    public List<String> generateBatchDocuments(DocumentTemplate template, List<Map<String, String>> batchValues) throws ManagerException {
        List<String> generatedFilePaths = new ArrayList<>();

        try {
            Path outputDirectory = resolveGeneratedDirectory();
            String currentDate = LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_FORMAT));

            int documentIndex = 1;
            for (Map<String, String> tokenValues : batchValues) {
                String renderedContent = renderTemplate(template, tokenValues);
                String sanitizedName = sanitizeForFileName(template.getTemplateName());
                String outputFileName = currentDate + "_" + sanitizedName + "_doc" + documentIndex + EXT_OUTPUT;

                Path outputFilePath = outputDirectory.resolve(outputFileName);
                try (FileWriter writer = new FileWriter(outputFilePath.toFile(), StandardCharsets.UTF_8)) {
                    writer.write(renderedContent);
                }

                generatedFilePaths.add(outputFilePath.toAbsolutePath().toString());
                documentIndex++;
            }
        } catch (IOException exception) {
            throw new ManagerException(MSG_GENERATE_ERROR + " Causa: " + exception.getMessage(), exception);
        }

        return generatedFilePaths;
    }


    public void deleteTemplate(String templateId) throws ManagerException {
        try {
            Path templateDirectory = resolveTemplateDirectory();
            Path metaPath = templateDirectory.resolve(templateId + EXT_META);
            Path bodyPath = templateDirectory.resolve(templateId + EXT_BODY);

            Files.deleteIfExists(metaPath);
            Files.deleteIfExists(bodyPath);

        } catch (IOException exception) {
            throw new ManagerException(MSG_DELETE_ERROR + " Causa: " + exception.getMessage(), exception);
        }
    }

    private Path resolveTemplateDirectory() throws IOException {
        Path directory = Paths.get(baseDirectory, DIR_DOCUMENTS, DIR_TEMPLATES);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        return directory;
    }

    private Path resolveGeneratedDirectory() throws IOException {
        Path directory = Paths.get(baseDirectory, DIR_DOCUMENTS, DIR_GENERATED);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
        return directory;
    }

    private String sanitizeForFileName(String name) {
        return name.replaceAll("[^a-zA-Z0-9áéíóúÁÉÍÓÚñÑ._-]", "_");
    }
}
