package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.dto.SelfEvaluation;
import mx.uv.fei.domain.enums.SelfEvaluationStatus;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Acceso a datos de las autoevaluaciones de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class SelfEvaluationDAO extends BaseDAO implements ISelfEvaluationDAO {

    private static final String DEFAULT_SELF_EVALUATION_STATUS = SelfEvaluationStatus.PENDING.getDatabaseValue();

    private static final String SQL_INSERT_SELF_EVALUATION =
            "INSERT INTO self_evaluation (q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, evidence, practitioner_id, report_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_SELF_EVALUATION =
            "UPDATE self_evaluation SET status = ?, evidence = ? WHERE self_eval_id = ?";
    private static final String SQL_UPDATE_SELF_EVALUATION_STATUS =
            "UPDATE self_evaluation SET status = ? WHERE self_eval_id = ?";
    private static final String SQL_REJECT_SELF_EVALUATION =
            "UPDATE self_evaluation SET status = ?, review_comment = ? WHERE self_eval_id = ?";
    private static final String SQL_UPDATE_SELF_EVALUATION_EVIDENCE =
            "UPDATE self_evaluation SET evidence = ? WHERE self_eval_id = ?";
    private static final String SQL_SELECT_SELF_EVALUATION_BY_REPORT =
            "SELECT * FROM self_evaluation WHERE report_id = ?";

    @Inject
    public SelfEvaluationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertSelfEvaluation(SelfEvaluation selfEvaluation) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_SELF_EVALUATION, Statement.RETURN_GENERATED_KEYS)) {

            bindSelfEvaluationAnswers(statement, selfEvaluation);
            statement.setString(11, selfEvaluation.getEvidence());
            statement.setInt(12, selfEvaluation.getPractitionerId());
            statement.setInt(13, selfEvaluation.getReportId());
            statement.setString(14, DEFAULT_SELF_EVALUATION_STATUS);

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar la autoevaluación en la base de datos.", e);
        }

        return generatedId;
    }

    @Override
    public SelfEvaluation getSelfEvaluationByReportId(int reportId) throws DAOException {
        SelfEvaluation recoveredSelfEvaluation = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_SELF_EVALUATION_BY_REPORT)) {

            statement.setInt(1, reportId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredSelfEvaluation = mapResultSetToSelfEvaluation(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar la autoevaluación por reporte.", e);
        }

        return recoveredSelfEvaluation;
    }

    @Override
    public void updateSelfEvaluation(SelfEvaluation selfEvaluation, int selfEvaluationId) throws DAOException {
        updateTuple(SQL_UPDATE_SELF_EVALUATION, statement -> {
            statement.setString(1, selfEvaluation.getStatus());
            statement.setString(2, selfEvaluation.getEvidence());
            statement.setInt(3, selfEvaluationId);
        });
    }

    public void updateSelfEvaluationStatus(int selfEvaluationId, String status) throws DAOException {
        updateTuple(SQL_UPDATE_SELF_EVALUATION_STATUS, statement -> {
            statement.setString(1, status);
            statement.setInt(2, selfEvaluationId);
        });
    }

    @Override
    public void rejectSelfEvaluation(int selfEvaluationId, String reviewComment) throws DAOException {
        updateTuple(SQL_REJECT_SELF_EVALUATION, statement -> {
            statement.setString(1, SelfEvaluationStatus.REJECTED.getDatabaseValue());
            statement.setString(2, reviewComment);
            statement.setInt(3, selfEvaluationId);
        });
    }

    public void updateSelfEvaluationEvidence(int selfEvaluationId, String evidence) throws DAOException {
        updateTuple(SQL_UPDATE_SELF_EVALUATION_EVIDENCE, statement -> {
            statement.setString(1, evidence);
            statement.setInt(2, selfEvaluationId);
        });
    }

    private void bindSelfEvaluationAnswers(PreparedStatement statement, SelfEvaluation selfEvaluation) throws SQLException {
        statement.setInt(1, selfEvaluation.getQ1());
        statement.setInt(2, selfEvaluation.getQ2());
        statement.setInt(3, selfEvaluation.getQ3());
        statement.setInt(4, selfEvaluation.getQ4());
        statement.setInt(5, selfEvaluation.getQ5());
        statement.setInt(6, selfEvaluation.getQ6());
        statement.setInt(7, selfEvaluation.getQ7());
        statement.setInt(8, selfEvaluation.getQ8());
        statement.setInt(9, selfEvaluation.getQ9());
        statement.setInt(10, selfEvaluation.getQ10());
    }

    private SelfEvaluation mapResultSetToSelfEvaluation(ResultSet resultSet) throws SQLException {
        SelfEvaluation selfEvaluation = new SelfEvaluation();
        selfEvaluation.setSelfEvalId(resultSet.getInt("self_eval_id"));
        selfEvaluation.setQ1(resultSet.getInt("q1"));
        selfEvaluation.setQ2(resultSet.getInt("q2"));
        selfEvaluation.setQ3(resultSet.getInt("q3"));
        selfEvaluation.setQ4(resultSet.getInt("q4"));
        selfEvaluation.setQ5(resultSet.getInt("q5"));
        selfEvaluation.setQ6(resultSet.getInt("q6"));
        selfEvaluation.setQ7(resultSet.getInt("q7"));
        selfEvaluation.setQ8(resultSet.getInt("q8"));
        selfEvaluation.setQ9(resultSet.getInt("q9"));
        selfEvaluation.setQ10(resultSet.getInt("q10"));
        selfEvaluation.setEvidence(resultSet.getString("evidence"));
        selfEvaluation.setPractitionerId(resultSet.getInt("practitioner_id"));
        selfEvaluation.setReportId(resultSet.getInt("report_id"));
        selfEvaluation.setStatus(resultSet.getString("status"));
        selfEvaluation.setReviewComment(resultSet.getString("review_comment"));
        return selfEvaluation;
    }
}