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

    private static final float MARGIN_LEFT = 50;
    private static final float HEADER_Y = 750;
    private static final float BODY_START_Y = 710;
    private static final float LINE_SPACING = 18f;
    private static final float HEADER_FONT_SIZE = 16;
    private static final float BODY_FONT_SIZE = 12;
    private static final float ANSWERS_FONT_SIZE = 10;
    private static final String DOCUMENT_TITLE = "PRAIS-03 - Autoevaluación del alumno";

    public static void generateSelfEvaluationPdf(SelfEvaluation selfEvaluation, User practitioner, File targetFile) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
                writeDocumentTitle(contentStream);
                writePractitionerInfo(contentStream, selfEvaluation, practitioner);
                writeAnswers(contentStream, selfEvaluation);
                writeFinalScore(contentStream, selfEvaluation);
                writeSignatureBlock(contentStream);
            }

            document.save(targetFile);
        }
    }

    private static void writeDocumentTitle(PDPageContentStream contentStream) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        contentStream.beginText();
        contentStream.setFont(fontBold, HEADER_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN_LEFT, HEADER_Y);
        contentStream.showText(DOCUMENT_TITLE);
        contentStream.endText();
    }

    private static void writePractitionerInfo(PDPageContentStream contentStream, SelfEvaluation selfEvaluation, User practitioner) throws IOException {
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        contentStream.beginText();
        contentStream.setFont(fontNormal, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN_LEFT, BODY_START_Y);
        contentStream.setLeading(LINE_SPACING);
        contentStream.showText("Practicante: " + practitioner.getName() + " " + practitioner.getLastName());
        contentStream.newLine();
        contentStream.showText("Matrícula/Usuario: " + practitioner.getUserName());
        contentStream.newLine();
        contentStream.showText("ID Reporte Final: " + selfEvaluation.getReportId());
        contentStream.newLine();
        contentStream.newLine();
        contentStream.endText();
    }

    private static void writeAnswers(PDPageContentStream contentStream, SelfEvaluation selfEvaluation) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        contentStream.beginText();
        contentStream.setFont(fontBold, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN_LEFT, BODY_START_Y - LINE_SPACING * 5);
        contentStream.setLeading(LINE_SPACING);
        contentStream.showText("Respuestas a las afirmaciones (1 al 5):");
        contentStream.newLine();

        contentStream.setFont(fontNormal, ANSWERS_FONT_SIZE);
        contentStream.showText("1. Participación productiva: " + selfEvaluation.getQ1()); contentStream.newLine();
        contentStream.showText("2. Aplicación de conocimientos teóricos/prácticos: " + selfEvaluation.getQ2()); contentStream.newLine();
        contentStream.showText("3. Me sentí seguro al realizar actividades: " + selfEvaluation.getQ3()); contentStream.newLine();
        contentStream.showText("4. Las actividades despertaron interés: " + selfEvaluation.getQ4()); contentStream.newLine();
        contentStream.showText("5. Se me proporcionó información y facilidades: " + selfEvaluation.getQ5()); contentStream.newLine();
        contentStream.showText("6. Se me dieron a conocer reglas internas: " + selfEvaluation.getQ6()); contentStream.newLine();
        contentStream.showText("7. Fui orientado correctamente: " + selfEvaluation.getQ7()); contentStream.newLine();
        contentStream.showText("8. Se realizó seguimiento efectivo: " + selfEvaluation.getQ8()); contentStream.newLine();
        contentStream.showText("9. Congruencia con mi carrera: " + selfEvaluation.getQ9()); contentStream.newLine();
        contentStream.showText("10. Las prácticas son importantes: " + selfEvaluation.getQ10()); contentStream.newLine();
        contentStream.endText();
    }

    private static void writeFinalScore(PDPageContentStream contentStream, SelfEvaluation selfEvaluation) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        contentStream.beginText();
        contentStream.setFont(fontBold, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN_LEFT, BODY_START_Y - LINE_SPACING * 17);
        contentStream.setLeading(LINE_SPACING);
        contentStream.showText("PUNTUACIÓN FINAL: " + selfEvaluation.getTotalScore());
        contentStream.endText();
    }

    private static void writeSignatureBlock(PDPageContentStream contentStream) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        contentStream.beginText();
        contentStream.setFont(fontBold, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(MARGIN_LEFT + 150, BODY_START_Y - LINE_SPACING * 22);
        contentStream.setLeading(LINE_SPACING);
        contentStream.showText("_________________________________________");
        contentStream.newLine();
        contentStream.newLineAtOffset(50, 0);
        contentStream.showText("NOMBRE Y FIRMA DEL ALUMNO");
        contentStream.endText();
    }
}