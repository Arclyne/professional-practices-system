package mx.uv.fei.domain.common;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDField;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.RandomAccessReadBuffer;
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

    private static final Logger logger = LoggerFactory.getLogger(PdfService.class);

    public void fillPdfTemplate(String templatePath, String outputPath, Map<String, String> fieldData) throws IOException {
        InputStream templateStream = null;
        File fileAttempt = new File(templatePath);

        try {
            if (fileAttempt.exists() && fileAttempt.canRead() && fileAttempt.isFile()) {
                templateStream = new FileInputStream(fileAttempt);
            } else {
                templateStream = getClass().getResourceAsStream(templatePath);

                if (templateStream == null) {
                    String cleanPath = templatePath.startsWith("/") ? templatePath.substring(1) : templatePath;
                    fileAttempt = Paths.get(System.getProperty("user.dir"), "app", "src", "main", "resources", cleanPath).toFile();

                    if (fileAttempt.exists() && fileAttempt.canRead()) {
                        templateStream = new FileInputStream(fileAttempt);
                    } else {
                        String reason = !fileAttempt.exists() ? "El archivo no existe" : "El archivo no tiene permisos de lectura";
                        throw new IOException(reason + " en la ruta: " + fileAttempt.getAbsolutePath());
                    }
                }
            }

            try (InputStream finalStream = templateStream;
                 PDDocument pdfDocument = Loader.loadPDF(new RandomAccessReadBuffer(finalStream))) {

                PDAcroForm acroForm = pdfDocument.getDocumentCatalog().getAcroForm();
                if (acroForm != null) {
                    for (Map.Entry<String, String> entry : fieldData.entrySet()) {
                        PDField field = acroForm.getField(entry.getKey());
                        if (field != null) {
                            field.setValue(entry.getValue());
                        }
                    }
                    acroForm.flatten();
                }
                pdfDocument.save(outputPath);
                logger.info("PDF generado exitosamente: {}", outputPath);
            }

        } catch (AccessDeniedException e) {
            logger.error("Error de permisos: No se puede leer el archivo en {}", fileAttempt != null ? fileAttempt.getAbsolutePath() : templatePath, e);
            throw e;
        } catch (IOException e) {
            logger.error("Error al acceder al template PDF. Ruta física intentada: {}",
                    (fileAttempt != null ? fileAttempt.getAbsolutePath() : "classpath:" + templatePath), e);
            throw e;
        }
    }
}