package mx.uv.fei.domain.common;

import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.dto.User;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;

public class SelfEvaluationPdfGenerator {

    public static void generateSelfEvaluationPdf(SelfEvaluation evaluation, User practitioner, File targetFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
                contentStream.newLineAtOffset(50, 750);
                contentStream.showText("PRAIS-03 - Autoevaluación del alumno");
                contentStream.endText();

                contentStream.beginText();
                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 12);
                contentStream.newLineAtOffset(50, 710);
                contentStream.setLeading(18f);
                contentStream.showText("Practicante: " + practitioner.getName() + " " + practitioner.getLastName());
                contentStream.newLine();
                contentStream.showText("Matrícula/Usuario: " + practitioner.getUserName());
                contentStream.newLine();
                contentStream.showText("ID Reporte Final: " + evaluation.getReportId());
                contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("Respuestas a las afirmaciones (1 al 5):");
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                contentStream.showText("1. Participación productiva: " + evaluation.getQ1()); contentStream.newLine();
                contentStream.showText("2. Aplicación de conocimientos teóricos/prácticos: " + evaluation.getQ2()); contentStream.newLine();
                contentStream.showText("3. Me sentí seguro al realizar actividades: " + evaluation.getQ3()); contentStream.newLine();
                contentStream.showText("4. Las actividades despertaron interés: " + evaluation.getQ4()); contentStream.newLine();
                contentStream.showText("5. Se me proporcionó información y facilidades: " + evaluation.getQ5()); contentStream.newLine();
                contentStream.showText("6. Se me dieron a conocer reglas internas: " + evaluation.getQ6()); contentStream.newLine();
                contentStream.showText("7. Fui orientado correctamente: " + evaluation.getQ7()); contentStream.newLine();
                contentStream.showText("8. Se realizó seguimiento efectivo: " + evaluation.getQ8()); contentStream.newLine();
                contentStream.showText("9. Congruencia con mi carrera: " + evaluation.getQ9()); contentStream.newLine();
                contentStream.showText("10. Las prácticas son importantes: " + evaluation.getQ10()); contentStream.newLine();
                contentStream.newLine();

                contentStream.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 12);
                contentStream.showText("PUNTUACIÓN FINAL: " + evaluation.getTotalScore());
                contentStream.newLine();

                // Línea de firma
                contentStream.newLineAtOffset(150, -100);
                contentStream.showText("_________________________________________");
                contentStream.newLine();
                contentStream.newLineAtOffset(50, 0);
                contentStream.showText("NOMBRE Y FIRMA DEL ALUMNO");
                contentStream.endText();
            }
            document.save(targetFile);
        }
    }
}