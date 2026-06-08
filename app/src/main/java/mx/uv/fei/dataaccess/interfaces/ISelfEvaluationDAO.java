package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.dataaccess.exceptions.DAOException;

public interface ISelfEvaluationDAO {
    int insertSelfEvaluation(SelfEvaluation evaluation) throws DAOException;
    SelfEvaluation getSelfEvaluationByReportId(int reportId) throws DAOException;
    boolean updateSelfEvaluation(SelfEvaluation evaluation, int selfEvalId) throws DAOException; // Nuevo
}