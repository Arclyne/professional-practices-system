package mx.uv.fei.domain.common;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.CoveredReport;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

@Component
public class ReportPdfGenerator {

    private static final float PAGE_MARGIN = 50;
    private static final float PAGE_WIDTH = PDRectangle.LETTER.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.LETTER.getHeight();
    private static final float LINE_HEIGHT_NORMAL = 20;
    private static final float LINE_HEIGHT_LARGE = 25;
    private static final float SECTION_SPACING = 30;
    private static final float SECTION_SPACING_LARGE = 60;
    private static final float HEADER_FONT_SIZE = 16;
    private static final float SUBHEADER_FONT_SIZE = 14;
    private static final float BODY_FONT_SIZE = 12;
    private static final float SMALL_FONT_SIZE = 10;
    private static final float DETAIL_FONT_SIZE = 11;
    private static final int TITLE_MAX_LENGTH = 55;
    private static final int TITLE_TRUNCATE_LENGTH = 52;
    private static final float MINIMUM_PAGE_CURSOR = 200;
    private static final float SIGNATURE_LINE_WIDTH = 150;
    private static final String DOWNLOADS_DIRECTORY = "Downloads";
    private static final String UNIVERSITY_NAME = "UNIVERSIDAD VERACRUZANA";
    private static final String FACULTY_NAME = "Facultad de Estadística e Informática - Licenciatura en Ingeniería de Software";

    public String generateMonthlyReportPdf(MonthlyReport report, User practitioner, List<Activity> activities) throws ManagerException {
        try (PDDocument document = new PDDocument()) {
            PDPageContentStream contentStream = createDocumentPage(document);

            try (contentStream) {
                float cursorY = PAGE_HEIGHT - PAGE_MARGIN;
                cursorY = writePageHeader(contentStream, cursorY,
                        "Reporte Mensual de Prácticas Profesionales");
                cursorY = writePractitionerInfo(contentStream, cursorY, practitioner,
                        "Periodo: " + report.getMonthName() + " " + report.getYear() +
                                " (" + report.getStartDate() + " al " + report.getEndDate() + ")");
                cursorY = writeActivitiesTable(contentStream, cursorY, activities);
                writeMonthlyReportSignatures(contentStream, cursorY, practitioner);
            }

            return saveDocument(document, "Reporte_" + report.getMonthName() + "_" + report.getYear() + ".pdf");

        } catch (IOException e) {
            throw new ManagerException("Error al generar el documento PDF.", e);
        }
    }

    public String generateProgressReportPdf(ProgressReport report, User practitioner,
                                            List<CoveredReport> coveredReports) throws ManagerException {
        try (PDDocument document = new PDDocument()) {
            PDPageContentStream contentStream = createDocumentPage(document);

            try (contentStream) {
                float cursorY = PAGE_HEIGHT - PAGE_MARGIN;
                cursorY = writePageHeader(contentStream, cursorY,
                        "Reporte de Avance: " + report.getReportType());
                cursorY = writePractitionerInfo(contentStream, cursorY, practitioner,
                        "Periodo cubierto: " + report.getPeriodCoveredStart() + " al " + report.getPeriodCoveredEnd(),
                        "Total de horas acumuladas: " + report.getTotalHoursAtSubmission() + " hrs");
                cursorY = writeCoveredReports(contentStream, cursorY, coveredReports);
                cursorY = writeProfessorFeedback(contentStream, cursorY, report);
                writeProgressReportSignatures(contentStream, cursorY);
            }

            return saveDocument(document, "Reporte_Avance_" + report.getReportType() + ".pdf");

        } catch (IOException e) {
            throw new ManagerException("Error al generar el documento PDF de avance.", e);
        }
    }

    private float writeCoveredReports(PDPageContentStream contentStream, float cursorY,
                                      List<CoveredReport> coveredReports) throws IOException {
        if (coveredReports == null || coveredReports.isEmpty()) {
            return cursorY;
        }
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        writeTextLine(contentStream, fontBold, BODY_FONT_SIZE, PAGE_MARGIN, cursorY,
                "Reportes mensuales cubiertos y sus actividades:");
        cursorY -= LINE_HEIGHT_NORMAL;

        for (CoveredReport coveredReport : coveredReports) {
            cursorY = writeCoveredReport(contentStream, cursorY, coveredReport);
            if (cursorY < MINIMUM_PAGE_CURSOR) {
                break;
            }
        }

        return cursorY - SECTION_SPACING;
    }

    private float writeCoveredReport(PDPageContentStream contentStream, float cursorY,
                                     CoveredReport coveredReport) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        MonthlyReport report = coveredReport.getReport();

        writeTextLine(contentStream, fontBold, DETAIL_FONT_SIZE, PAGE_MARGIN, cursorY,
                report.getMonthName() + " " + report.getYear()
                        + " (" + report.getStartDate() + " al " + report.getEndDate() + ")");
        cursorY -= LINE_HEIGHT_NORMAL;

        for (Activity activity : coveredReport.getActivities()) {
            writeTextLine(contentStream, fontNormal, SMALL_FONT_SIZE, PAGE_MARGIN + 15, cursorY,
                    "- " + formatDateRange(activity) + ": " + truncateTitle(activity.getTitle())
                            + " (" + activity.getDurationHours() + " hrs)");
            cursorY -= LINE_HEIGHT_NORMAL;
            if (cursorY < MINIMUM_PAGE_CURSOR) {
                break;
            }
        }

        return cursorY;
    }

    private PDPageContentStream createDocumentPage(PDDocument document) throws IOException {
        PDPage page = new PDPage(PDRectangle.LETTER);
        document.addPage(page);
        return new PDPageContentStream(document, page);
    }

    private float writePageHeader(PDPageContentStream contentStream, float cursorY, String reportTitle) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        writeTextLine(contentStream, fontBold, HEADER_FONT_SIZE, PAGE_MARGIN, cursorY, UNIVERSITY_NAME);
        cursorY -= LINE_HEIGHT_NORMAL;

        writeTextLine(contentStream, fontNormal, BODY_FONT_SIZE, PAGE_MARGIN, cursorY, FACULTY_NAME);
        cursorY -= SECTION_SPACING;

        writeTextLine(contentStream, fontBold, SUBHEADER_FONT_SIZE, PAGE_MARGIN, cursorY, reportTitle);
        cursorY -= LINE_HEIGHT_LARGE;

        return cursorY;
    }

    private float writePractitionerInfo(PDPageContentStream contentStream, float cursorY, User practitioner, String... additionalLines) throws IOException {
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        contentStream.beginText();
        contentStream.setFont(fontNormal, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_MARGIN, cursorY);
        contentStream.showText("Practicante: " + practitioner.getName() + " " + practitioner.getLastName());

        for (String line : additionalLines) {
            contentStream.newLineAtOffset(0, -15);
            contentStream.showText(line);
        }
        contentStream.endText();

        return cursorY - (15 * additionalLines.length) - LINE_HEIGHT_LARGE;
    }

    private float writeActivitiesTable(PDPageContentStream contentStream, float cursorY, List<Activity> activities) throws IOException {
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        drawHorizontalLine(contentStream, cursorY + 15);
        writeTableHeader(contentStream, fontBold, cursorY);
        cursorY -= 10;
        drawHorizontalLine(contentStream, cursorY);
        cursorY -= LINE_HEIGHT_NORMAL;

        contentStream.setFont(fontNormal, SMALL_FONT_SIZE);
        int totalHours = 0;

        for (Activity activity : activities) {
            contentStream.beginText();
            contentStream.newLineAtOffset(PAGE_MARGIN, cursorY);
            contentStream.showText(formatDateRange(activity));
            contentStream.newLineAtOffset(150, 0);
            contentStream.showText(truncateTitle(activity.getTitle()));
            contentStream.newLineAtOffset(290, 0);
            contentStream.showText(String.valueOf(activity.getDurationHours()));
            contentStream.endText();

            totalHours += activity.getDurationHours();
            cursorY -= LINE_HEIGHT_NORMAL;

            if (cursorY < MINIMUM_PAGE_CURSOR) {
                break;
            }
        }

        drawHorizontalLine(contentStream, cursorY + 10);
        cursorY -= 15;
        writeTotalHours(contentStream, fontBold, cursorY, totalHours);

        return cursorY - SECTION_SPACING_LARGE;
    }

    private void writeTableHeader(PDPageContentStream contentStream, PDType1Font fontBold, float cursorY) throws IOException {
        contentStream.beginText();
        contentStream.setFont(fontBold, BODY_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_MARGIN, cursorY);
        contentStream.showText("Fechas");
        contentStream.newLineAtOffset(150, 0);
        contentStream.showText("Actividad y Descripción");
        contentStream.newLineAtOffset(290, 0);
        contentStream.showText("Horas");
        contentStream.endText();
    }

    private void writeTotalHours(PDPageContentStream contentStream, PDType1Font fontBold, float cursorY, int totalHours) throws IOException {
        contentStream.beginText();
        contentStream.setFont(fontBold, SMALL_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_WIDTH - PAGE_MARGIN - SECTION_SPACING_LARGE, cursorY);
        contentStream.showText("Total: " + totalHours + " hrs");
        contentStream.endText();
    }

    private float writeProfessorFeedback(PDPageContentStream contentStream, float cursorY, ProgressReport report) throws IOException {
        if (report.getProfessorFeedback() == null || report.getProfessorFeedback().isEmpty()) {
            return cursorY;
        }
        PDType1Font fontBold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        writeTextLine(contentStream, fontBold, BODY_FONT_SIZE, PAGE_MARGIN, cursorY, "Observaciones del Profesor:");
        cursorY -= LINE_HEIGHT_NORMAL;
        writeTextLine(contentStream, fontNormal, DETAIL_FONT_SIZE, PAGE_MARGIN, cursorY, report.getProfessorFeedback());

        return cursorY - SECTION_SPACING_LARGE;
    }

    private void writeMonthlyReportSignatures(PDPageContentStream contentStream, float cursorY, User practitioner) throws IOException {
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

        drawSignatureLine(contentStream, PAGE_MARGIN, cursorY);
        contentStream.beginText();
        contentStream.setFont(fontNormal, SMALL_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_MARGIN + 15, cursorY - 15);
        contentStream.showText("Organización Receptora");
        contentStream.newLineAtOffset(15, -12);
        contentStream.showText("Firma y Sello");
        contentStream.endText();

        drawSignatureLine(contentStream, PAGE_WIDTH - PAGE_MARGIN - SIGNATURE_LINE_WIDTH, cursorY);
        contentStream.beginText();
        contentStream.setFont(fontNormal, SMALL_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_WIDTH - PAGE_MARGIN - 145, cursorY - 15);
        contentStream.showText("Firma del Practicante");
        contentStream.newLineAtOffset(5, -12);
        contentStream.showText(practitioner.getName() + " " + practitioner.getLastName());
        contentStream.endText();
    }

    private void writeProgressReportSignatures(PDPageContentStream contentStream, float cursorY) throws IOException {
        PDType1Font fontNormal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        float signatureY = cursorY - 100;

        drawSignatureLine(contentStream, PAGE_MARGIN, signatureY);
        contentStream.beginText();
        contentStream.setFont(fontNormal, SMALL_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_MARGIN + 15, signatureY - 15);
        contentStream.showText("Firma del Profesor");
        contentStream.endText();

        drawSignatureLine(contentStream, PAGE_WIDTH - PAGE_MARGIN - SIGNATURE_LINE_WIDTH, signatureY);
        contentStream.beginText();
        contentStream.setFont(fontNormal, SMALL_FONT_SIZE);
        contentStream.newLineAtOffset(PAGE_WIDTH - PAGE_MARGIN - 130, signatureY - 15);
        contentStream.showText("Firma del Practicante");
        contentStream.endText();
    }

    private void writeTextLine(PDPageContentStream contentStream, PDType1Font font, float fontSize, float x, float y, String text) throws IOException {
        contentStream.beginText();
        contentStream.setFont(font, fontSize);
        contentStream.newLineAtOffset(x, y);
        contentStream.showText(text);
        contentStream.endText();
    }

    private void drawHorizontalLine(PDPageContentStream contentStream, float y) throws IOException {
        contentStream.setLineWidth(1f);
        contentStream.moveTo(PAGE_MARGIN, y);
        contentStream.lineTo(PAGE_WIDTH - PAGE_MARGIN, y);
        contentStream.stroke();
    }

    private void drawSignatureLine(PDPageContentStream contentStream, float x, float y) throws IOException {
        contentStream.moveTo(x, y);
        contentStream.lineTo(x + SIGNATURE_LINE_WIDTH, y);
        contentStream.stroke();
    }

    private String formatDateRange(Activity activity) {
        return activity.getStartDate() + " al " + activity.getEndDate();
    }

    private String truncateTitle(String title) {
        return title.length() > TITLE_MAX_LENGTH ? title.substring(0, TITLE_TRUNCATE_LENGTH) + "..." : title;
    }

    private String saveDocument(PDDocument document, String fileName) throws IOException {
        String homeDirectory = System.getProperty("user.home");
        File outputFile = Paths.get(homeDirectory, DOWNLOADS_DIRECTORY, fileName).toFile();
        document.save(outputFile);
        return outputFile.getAbsolutePath();
    }
}