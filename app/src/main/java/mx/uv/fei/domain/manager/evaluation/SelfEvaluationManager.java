package mx.uv.fei.domain.manager.evaluation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.common.PersistenceErrorTranslator;
import mx.uv.fei.domain.common.validators.ReportValidator;
import mx.uv.fei.domain.common.validators.SelfEvaluationValidator;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PracticeAccessManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class SelfEvaluationManager {

    private static final Logger log = LoggerFactory.getLogger(SelfEvaluationManager.class);
    private static final String SELF_EVALUATION_STATUS_REVIEWED = "Revisada";

    private final ISelfEvaluationDAO selfEvaluationDAO;
    private final PracticeAccessManager practiceAccessManager;

    @Inject
    public SelfEvaluationManager(ISelfEvaluationDAO selfEvaluationDAO, PracticeAccessManager practiceAccessManager) {
        this.selfEvaluationDAO = selfEvaluationDAO;
        this.practiceAccessManager = practiceAccessManager;
    }

    public void registerSelfEvaluation(SelfEvaluation selfEvaluation) throws ManagerException {
        practiceAccessManager.ensureSubmissionsAllowed(selfEvaluation.getPractitionerId());
        SelfEvaluationValidator.validateEvaluationData(selfEvaluation);
        try {
            int generatedId = selfEvaluationDAO.insertSelfEvaluation(selfEvaluation);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar la autoevaluación.");
            }
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw PersistenceErrorTranslator.translate(e);
        }
    }

    public void submitEvidence(int selfEvaluationId, String fileUrl) throws ManagerException {
        ReportValidator.validateSignedReport(fileUrl);
        try {
            selfEvaluationDAO.updateSelfEvaluationEvidence(selfEvaluationId, fileUrl);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al adjuntar evidencia.", e);
        }
    }

    public void updateSelfEvaluation(SelfEvaluation selfEvaluation, int selfEvaluationId) throws ManagerException {
        try {
            selfEvaluationDAO.updateSelfEvaluation(selfEvaluation, selfEvaluationId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al actualizar la autoevaluación.", e);
        }
    }

    public void updateSelfEvaluationStatus(int selfEvaluationId, String status) throws ManagerException {
        try {
            selfEvaluationDAO.updateSelfEvaluationStatus(selfEvaluationId, status);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error al actualizar la autoevaluación.", e);
        }
    }

    public void markAsReviewed(int selfEvaluationId) throws ManagerException {
        updateSelfEvaluationStatus(selfEvaluationId, SELF_EVALUATION_STATUS_REVIEWED);
    }

    public SelfEvaluation recoverSelfEvaluation(int reportId) throws ManagerException {
        try {
            return selfEvaluationDAO.getSelfEvaluationByReportId(reportId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al recuperar la autoevaluación.", e);
        }
    }
}