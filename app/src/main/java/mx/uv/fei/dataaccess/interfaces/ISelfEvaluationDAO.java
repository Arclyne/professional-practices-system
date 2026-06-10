package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.SelfEvaluation;

public interface ISelfEvaluationDAO {

    int insertSelfEvaluation(SelfEvaluation selfEvaluation) throws DAOException;

    SelfEvaluation getSelfEvaluationByReportId(int reportId) throws DAOException;

    boolean updateSelfEvaluation(SelfEvaluation selfEvaluation, int selfEvaluationId) throws DAOException;

    boolean updateSelfEvaluationStatus(int selfEvaluationId, String status) throws DAOException;

    boolean updateSelfEvaluationEvidence(int selfEvaluationId, String evidence) throws DAOException;
}