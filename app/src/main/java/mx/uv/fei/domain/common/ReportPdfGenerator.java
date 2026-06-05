package mx.uv.fei.domain.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class ReportPdfGenerator {

    private static final float MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();

    public String generatePdf(MonthlyReport report, User practitioner, List<Activity> activities) throws ManagerException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.LETTER);
            document.addPage(page);

            PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
            PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

            try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {

                float cursorY = PAGE_HEIGHT - MARGIN;

                contentStream.beginText();
                contentStream.setFont(fontBold, 16);
                contentStream.newLineAtOffset(MARGIN, cursorY);
                contentStream.showText("UNIVERSIDAD VERACRUZANA");
                contentStream.endText();
                cursorY -= 20;

                contentStream.beginText();
                contentStream.setFont(fontNormal, 12);
                contentStream.newLineAtOffset(MARGIN, cursorY);
                contentStream.showText("Facultad de Estadística e Informática - Licenciatura en Ingeniería de Software");
                contentStream.endText();
                cursorY -= 30;

                contentStream.beginText();
                contentStream.setFont(fontBold, 14);
                contentStream.newLineAtOffset(MARGIN, cursorY);
                contentStream.showText("Reporte Mensual de Prácticas Profesionales");
                contentStream.endText();
                cursorY -= 25;

                contentStream.beginText();
                contentStream.setFont(fontNormal, 12);
                contentStream.newLineAtOffset(MARGIN, cursorY);
                contentStream.showText("Practicante: " + practitioner.getName() + " " + practitioner.getLastName());
                contentStream.newLineAtOffset(0, -15);
                contentStream.showText("Periodo: " + report.getMonthName() + " " + report.getYear() + " (" + report.getStartDate() + " al " + report.getEndDate() + ")");
                contentStream.endText();
                cursorY -= 40;

                contentStream.setLineWidth(1f);
                contentStream.moveTo(MARGIN, cursorY + 15);
                contentStream.lineTo(PAGE_WIDTH - MARGIN, cursorY + 15);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(fontBold, 12);
                contentStream.newLineAtOffset(MARGIN, cursorY);
                contentStream.showText("Fecha");
                contentStream.newLineAtOffset(100, 0);
                contentStream.showText("Actividad y Descripción");
                contentStream.newLineAtOffset(320, 0);
                contentStream.showText("Horas");
                contentStream.endText();
                cursorY -= 10;

                contentStream.moveTo(MARGIN, cursorY);
                contentStream.lineTo(PAGE_WIDTH - MARGIN, cursorY);
                contentStream.stroke();
                cursorY -= 20;

                contentStream.setFont(fontNormal, 10);
                int totalHours = 0;

                for (Activity activity : activities) {
                    contentStream.beginText();
                    contentStream.newLineAtOffset(MARGIN, cursorY);
                    contentStream.showText(activity.getActivityDate().toString());

                    contentStream.newLineAtOffset(100, 0);
                    String shortTitle = activity.getTitle().length() > 55 ? activity.getTitle().substring(0, 52) + "..." : activity.getTitle();
                    contentStream.showText(shortTitle);

                    contentStream.newLineAtOffset(320, 0);
                    contentStream.showText(String.valueOf(activity.getDurationHours()));
                    contentStream.endText();

                    totalHours += activity.getDurationHours();
                    cursorY -= 20;

                    if (cursorY < 200) break;
                }

                contentStream.moveTo(MARGIN, cursorY + 10);
                contentStream.lineTo(PAGE_WIDTH - MARGIN, cursorY + 10);
                contentStream.stroke();
                cursorY -= 15;

                contentStream.beginText();
                contentStream.setFont(fontBold, 10);
                contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 60, cursorY);
                contentStream.showText("Total: " + totalHours + " hrs");
                contentStream.endText();
                cursorY -= 80;

                float signatureY = cursorY;

                contentStream.moveTo(MARGIN, signatureY);
                contentStream.lineTo(MARGIN + 150, signatureY);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(fontNormal, 10);
                contentStream.newLineAtOffset(MARGIN + 15, signatureY - 15);
                contentStream.showText("Organización Receptora");
                contentStream.newLineAtOffset(15, -12);
                contentStream.showText("Firma y Sello");
                contentStream.endText();

                contentStream.moveTo(PAGE_WIDTH - MARGIN - 150, signatureY);
                contentStream.lineTo(PAGE_WIDTH - MARGIN, signatureY);
                contentStream.stroke();

                contentStream.beginText();
                contentStream.setFont(fontNormal, 10);
                contentStream.newLineAtOffset(PAGE_WIDTH - MARGIN - 145, signatureY - 15);
                contentStream.showText("Firma del Practicante");
                contentStream.newLineAtOffset(5, -12);
                contentStream.showText(practitioner.getName() + " " + practitioner.getLastName());
                contentStream.endText();
            }

            String home = System.getProperty("user.home");
            String fileName = "Reporte_" + report.getMonthName() + "_" + report.getYear() + ".pdf";
            File outputFile = Paths.get(home, "Downloads", fileName).toFile();

            document.save(outputFile);
            return outputFile.getAbsolutePath();

        } catch (IOException e) {
            throw new ManagerException("Error al generar el documento PDF.", e);
        }
    }
}