package mx.uv.fei.domain.common;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Map;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.interactive.form.PDAcroForm;
import org.apache.pdfbox.pdmodel.interactive.form.PDTextField;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class PdfServiceTest {

    private PdfService pdfService;

    @TempDir
    Path tempDir;

    private String validTemplatePath;
    private String outputPdfPath;

    @BeforeEach
    void setUp() throws IOException {
        pdfService = new PdfService();

        File tempPdf = tempDir.resolve("template.pdf").toFile();
        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage();
            doc.addPage(page);

            PDAcroForm acroForm = new PDAcroForm(doc);
            doc.getDocumentCatalog().setAcroForm(acroForm);

            PDTextField textBox = new PDTextField(acroForm);
            textBox.setPartialName("NombrePracticante");
            acroForm.getFields().add(textBox);

            doc.save(tempPdf);
        }

        validTemplatePath = tempPdf.getAbsolutePath();
        outputPdfPath = tempDir.resolve("output_filled.pdf").toAbsolutePath().toString();
    }

    @Test
    void fillPdfTemplate_ValidTemplateAndData_CreatesFilledPdf() {
        Map<String, String> data = Map.of("NombrePracticante", "Angel Aguilar");

        assertDoesNotThrow(() -> pdfService.fillPdfTemplate(validTemplatePath, outputPdfPath, data));
    }

    @Test
    void fillPdfTemplate_NonExistentFile_ThrowsIOException() {

        String badPath = "plantilla_que_no_existe.pdf";

        String outputPath = tempDir.resolve("salida.pdf").toAbsolutePath().toString();

        Map<String, String> data = Map.of("NombrePracticante", "Angel Aguilar");

        assertThrows(IOException.class, () -> {
            pdfService.fillPdfTemplate(badPath, outputPath, data);
        });
    }
}
