package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.common.validators.SelfEvaluationValidator;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SelfEvaluationManager {

    private static final Logger logger = LoggerFactory.getLogger(SelfEvaluationManager.class);

    private static final String MSG_REGISTER_ERROR = "No se pudo registrar la autoevaluación.";
    private static final String MSG_UPDATE_ERROR = "Error al actualizar la autoevaluación.";

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
            logger.error(e.getMessage(), e);
            throw new ManagerException(MSG_REGISTER_ERROR + " Causa: " + e.getMessage(), e);
        }
    }

    public void submitEvidence(int evalId, String fileUrl) throws ManagerException {
        ReportValidator.validateSignedReport(fileUrl);

        try {
            boolean isUpdated = selfEvaluationDAO.updateEvidence(evalId, fileUrl);
            if (!isUpdated) throw new ManagerException(MSG_UPDATE_ERROR);
        } catch (DAOException e) {
            throw new ManagerException("Error al adjuntar evidencia. Causa: " + e.getMessage(), e);
        }
    }

    public void updateSelfEvaluation(SelfEvaluation evaluation, int evalId) throws ManagerException {
        try {
            boolean isUpdated = selfEvaluationDAO.updateSelfEvaluation(evaluation, evalId);
            if (!isUpdated) throw new ManagerException(MSG_UPDATE_ERROR);
        } catch (DAOException e) {
            throw new ManagerException(MSG_UPDATE_ERROR + " Causa: " + e.getMessage(), e);
        }
    }

    public void updateStatus(int evalId, String status) throws ManagerException {
        try {
            boolean isUpdated = selfEvaluationDAO.updateStatus(evalId, status);
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