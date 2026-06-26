package mx.uv.fei.domain.manager.evaluation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.dto.GradingEligibility;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.enums.SelfEvaluationStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Determina si un practicante cumple los requisitos de cierre para ser calificado.
 *
 * Un practicante es calificable cuando su carta de liberación (documentos finales) fue
 * aceptada, su reporte final fue evaluado (horas cumplidas) y su autoevaluación fue aceptada.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
@Component
public class GradingEligibilityManager {

    private static final Logger log = LoggerFactory.getLogger(GradingEligibilityManager.class);

    private final IPractitionerDocumentDAO documentDAO;
    private final IProgressReportDAO progressReportDAO;
    private final ISelfEvaluationDAO selfEvaluationDAO;

    @Inject
    public GradingEligibilityManager(IPractitionerDocumentDAO documentDAO, IProgressReportDAO progressReportDAO,
                                     ISelfEvaluationDAO selfEvaluationDAO) {
        this.documentDAO = documentDAO;
        this.progressReportDAO = progressReportDAO;
        this.selfEvaluationDAO = selfEvaluationDAO;
    }

    public GradingEligibility evaluateEligibility(int practitionerId) throws ManagerException {
        try {
            boolean finalDocumentsAccepted = documentDAO.areAllDocumentsAccepted(
                    practitionerId, DocumentCategory.FINAL.getDatabaseValue());
            ProgressReport finalReport = progressReportDAO.getProgressReportByPractitionerAndType(
                    practitionerId, ProgressReportType.FINAL.getDatabaseValue());
            boolean finalReportAccepted = isReportEvaluated(finalReport);
            boolean selfEvaluationAccepted = isSelfEvaluationAccepted(finalReport);

            return new GradingEligibility(finalDocumentsAccepted, finalReportAccepted, selfEvaluationAccepted);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudieron verificar los requisitos de calificación.", e);
        }
    }

    public String buildPendingRequirementsMessage(GradingEligibility eligibility) {
        List<String> pendingRequirements = new ArrayList<>();
        if (!eligibility.finalDocumentsAccepted()) {
            pendingRequirements.add("documentos finales aceptados (carta de liberación y evaluaciones)");
        }
        if (!eligibility.finalReportAccepted()) {
            pendingRequirements.add("reporte final aceptado (horas cumplidas)");
        }
        if (!eligibility.selfEvaluationAccepted()) {
            pendingRequirements.add("autoevaluación aceptada");
        }

        return "No puedes calificar todavía. Requisitos pendientes: " + String.join(", ", pendingRequirements) + ".";
    }

    private boolean isReportEvaluated(ProgressReport finalReport) {
        return finalReport != null
                && ReportStatus.EVALUATED.getDatabaseValue().equals(finalReport.getStatus());
    }

    private boolean isSelfEvaluationAccepted(ProgressReport finalReport) throws DAOException {
        boolean isAccepted = false;

        if (finalReport != null) {
            SelfEvaluation selfEvaluation = selfEvaluationDAO.getSelfEvaluationByReportId(finalReport.getReportId());
            isAccepted = selfEvaluation != null
                    && SelfEvaluationStatus.REVIEWED.getDatabaseValue().equals(selfEvaluation.getStatus());
        }

        return isAccepted;
    }
}
