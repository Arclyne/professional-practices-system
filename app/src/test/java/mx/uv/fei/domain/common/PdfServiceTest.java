package mx.uv.fei.domain.common;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.File;
import java.io.IOException;
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

    private static final String PRACTITIONER_FIELD_NAME = "NombrePracticante";
    private static final String PRACTITIONER_FULL_NAME = "Angel Gabriel Aguilar Hernandez";

    @TempDir
    Path temporaryDirectory;

    private PdfService pdfService;
    private String validTemplatePath;
    private String outputPdfPath;

    @BeforeEach
    void setUp() throws IOException {
        pdfService = new PdfService();

        File templatePdfFile = temporaryDirectory.resolve("plantilla_oficio.pdf").toFile();
        try (PDDocument templateDocument = new PDDocument()) {
            PDPage templatePage = new PDPage();
            templateDocument.addPage(templatePage);

            PDAcroForm acroForm = new PDAcroForm(templateDocument);
            templateDocument.getDocumentCatalog().setAcroForm(acroForm);

            PDTextField practitionerNameField = new PDTextField(acroForm);
            practitionerNameField.setPartialName(PRACTITIONER_FIELD_NAME);
            acroForm.getFields().add(practitionerNameField);

            templateDocument.save(templatePdfFile);
        }

        validTemplatePath = templatePdfFile.getAbsolutePath();
        outputPdfPath = temporaryDirectory.resolve("oficio_generado.pdf").toAbsolutePath().toString();
    }

    @Test
    void fillPdfTemplate_ValidTemplateAndData_CreatesFilledPdf() {
        Map<String, String> templateData = Map.of(PRACTITIONER_FIELD_NAME, PRACTITIONER_FULL_NAME);

        assertDoesNotThrow(() -> pdfService.fillPdfTemplate(validTemplatePath, outputPdfPath, templateData));
    }

    @Test
    void fillPdfTemplate_NonExistentFile_ThrowsIOException() {
        String nonExistentTemplatePath = "plantilla_inexistente.pdf";
        Map<String, String> templateData = Map.of(PRACTITIONER_FIELD_NAME, PRACTITIONER_FULL_NAME);

        assertThrows(IOException.class,
                () -> pdfService.fillPdfTemplate(nonExistentTemplatePath, outputPdfPath, templateData));
    }
}
