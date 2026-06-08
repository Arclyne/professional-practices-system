package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.common.validators.SelfEvaluationValidator;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class SelfEvaluationManager {

    private static final String MSG_REGISTER_ERROR = "No se pudo registrar la autoevaluación.";
    private static final String MSG_UPDATE_ERROR = "Error al actualizar el estado de la autoevaluación.";

    private final ISelfEvaluationDAO selfEvaluationDAO;

    @Inject
    public SelfEvaluationManager(ISelfEvaluationDAO selfEvaluationDAO) {
        this.selfEvaluationDAO = selfEvaluationDAO;
    }

    public void registerSelfEvaluation(SelfEvaluation evaluation) throws ManagerException {
        SelfEvaluationValidator.validateEvaluationData(evaluation);

        try {
            int resultId = selfEvaluationDAO.insertSelfEvaluation(evaluation);
            if (resultId <= 0) {
                throw new ManagerException(MSG_REGISTER_ERROR);
            }
        } catch (DAOException e) {
            throw new ManagerException(MSG_REGISTER_ERROR + " Causa: " + e.getMessage(), e);
        }
    }

    public void submitEvidence(int evalId, String fileUrl) throws ManagerException {
        ReportValidator.validateSignedReport(fileUrl);

        try {
            SelfEvaluation eval = selfEvaluationDAO.getSelfEvaluationByReportId(evalId);
            if (eval == null) throw new ManagerException("La autoevaluación no existe.");

            eval.setEvidence(fileUrl);
            boolean isUpdated = selfEvaluationDAO.updateSelfEvaluation(eval, evalId);
            if (!isUpdated) throw new ManagerException(MSG_UPDATE_ERROR);
        } catch (DAOException e) {
            throw new ManagerException("Error al adjuntar evidencia. Causa: " + e.getMessage(), e);
        }
    }

    public void markAsReviewed(int evalId) throws ManagerException {
        try {
            SelfEvaluation eval = selfEvaluationDAO.getSelfEvaluationByReportId(evalId);
            if (eval == null) throw new ManagerException("La autoevaluación no existe.");

            eval.setStatus("Revisada");
            boolean isUpdated = selfEvaluationDAO.updateSelfEvaluation(eval, evalId);
            if (!isUpdated) throw new ManagerException(MSG_UPDATE_ERROR);
        } catch (DAOException e) {
            throw new ManagerException(MSG_UPDATE_ERROR + " Causa: " + e.getMessage(), e);
        }
    }

    public SelfEvaluation recoverSelfEvaluation(int currentReportId) throws ManagerException {
        try {
            return selfEvaluationDAO.getSelfEvaluationByReportId(currentReportId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar la autoevaluación: " + e.getMessage(), e);
        }
    }
}