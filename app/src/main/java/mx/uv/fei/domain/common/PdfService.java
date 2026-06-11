package mx.uv.fei.domain.common;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;
import java.nio.file.Paths;
import java.util.Map;

public class PdfService {

    private static final Logger log = LoggerFactory.getLogger(PdfService.class);
    private static final String RESOURCE_BASE_PATH = "app/src/main/resources";

    public void fillPdfTemplate(String templatePath, String outputPath, Map<String, String> fieldData) throws IOException {
        try (InputStream templateStream = resolveTemplateStream(templatePath);
             PDDocument pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(templateStream))) {

            fillFormFields(pdfDocument, fieldData);
            pdfDocument.save(outputPath);
            log.info("PDF generado exitosamente: {}", outputPath);

        } catch (AccessDeniedException e) {
            log.error("Error de permisos al leer la plantilla PDF en: {}", templatePath, e);
            throw e;
        } catch (IOException e) {
            log.error("Error al acceder a la plantilla PDF en: {}", templatePath, e);
            throw e;
        }
    }

    private InputStream resolveTemplateStream(String templatePath) throws IOException {
        File templateFile = new File(templatePath);
        if (templateFile.exists() && templateFile.canRead() && templateFile.isFile()) {
            return new FileInputStream(templateFile);
        }

        InputStream classpathStream = getClass().getResourceAsStream(templatePath);
        if (classpathStream != null) {
            return classpathStream;
        }

        return resolveFromResourceDirectory(templatePath);
    }

    private InputStream resolveFromResourceDirectory(String templatePath) throws IOException {
        String cleanPath = templatePath.startsWith("/") ? templatePath.substring(1) : templatePath;
        File resourceFile = Paths.get(System.getProperty("user.dir"), RESOURCE_BASE_PATH, cleanPath).toFile();

        if (!resourceFile.exists()) {
            throw new IOException("El archivo no existe en la ruta: " + resourceFile.getAbsolutePath());
        }
        if (!resourceFile.canRead()) {
            throw new AccessDeniedException("El archivo no tiene permisos de lectura en la ruta: " + resourceFile.getAbsolutePath());
        }

        return new FileInputStream(resourceFile);
    }

    private void fillFormFields(PDDocument pdfDocument, Map<String, String> fieldData) throws IOException {
        PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
        if (acroForm != null) {
            for (Map.Entry<String, String> fieldEntry : fieldData.entrySet()) {
                PDField field = acroForm.getField(fieldEntry.getKey());
                if (field != null) {
                    field.setValue(fieldEntry.getValue());
                }
            }
            acroForm.flatten();
        }
    }
}