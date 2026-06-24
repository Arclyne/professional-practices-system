package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.dataaccess.interfaces.IProgressReportDAO;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.dto.GradingEligibility;
import mx.uv.fei.domain.dto.ProgressReport;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.enums.DocumentType;
import mx.uv.fei.domain.enums.ProgressReportType;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

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

    private static final String SELF_EVALUATION_STATUS_ACCEPTED = "Revisada";

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
                    practitionerId, DocumentType.FINAL.getDatabaseValue());
            ProgressReport finalReport = progressReportDAO.getProgressReportByPractitionerAndType(
                    practitionerId, ProgressReportType.FINAL.getDatabaseValue());
            boolean finalReportAccepted = isReportEvaluated(finalReport);
            boolean selfEvaluationAccepted = isSelfEvaluationAccepted(finalReport);

            return new GradingEligibility(finalDocumentsAccepted, finalReportAccepted, selfEvaluationAccepted);
        } catch (DAOException e) {
            throw new ManagerException("No se pudieron verificar los requisitos de calificación.", e);
        }
    }

    public String buildPendingRequirementsMessage(GradingEligibility eligibility) {
        List<String> pendingRequirements = new ArrayList<>();
        if (!eligibility.finalDocumentsAccepted()) {
            pendingRequirements.add("carta de liberación aceptada");
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
                    && SELF_EVALUATION_STATUS_ACCEPTED.equals(selfEvaluation.getStatus());
        }

        return isAccepted;
    }
}
