package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISelfEvaluationDAO;
import mx.uv.fei.domain.dto.SelfEvaluation;

import java.sql.*;

@Component
public class SelfEvaluationDAO extends BaseDAO implements ISelfEvaluationDAO {

    private static final String SQL_INSERT = "INSERT INTO self_evaluation (q1, q2, q3, q4, q5, q6, q7, q8, q9, q10, evidence, practitioner_id, report_id, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE = "UPDATE self_evaluation SET status = ? WHERE self_eval_id = ?";
    private static final String SQL_SELECT_BY_REPORT = "SELECT * FROM self_evaluation WHERE report_id = ?";

    @Inject
    public SelfEvaluationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertSelfEvaluation(SelfEvaluation evaluation) throws DAOException {
        int generatedId = -1;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, evaluation.getQ1());
            statement.setInt(2, evaluation.getQ2());
            statement.setInt(3, evaluation.getQ3());
            statement.setInt(4, evaluation.getQ4());
            statement.setInt(5, evaluation.getQ5());
            statement.setInt(6, evaluation.getQ6());
            statement.setInt(7, evaluation.getQ7());
            statement.setInt(8, evaluation.getQ8());
            statement.setInt(9, evaluation.getQ9());
            statement.setInt(10, evaluation.getQ10());
            statement.setString(11, evaluation.getEvidence());
            statement.setInt(12, evaluation.getPractitionerId());
            statement.setInt(13, evaluation.getReportId());
            statement.setString(14, "Pendiente");

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
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_REPORT)) {

            statement.setInt(1, reportId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return mapResultSetToSelfEvaluation(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar la autoevaluación por reporte.", e);
        }
        return null;
    }

    @Override
    public boolean updateSelfEvaluation(SelfEvaluation evaluation, int selfEvalId) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, evaluation.getStatus());
            statement.setInt(2, selfEvalId);
        });
    }

    private SelfEvaluation mapResultSetToSelfEvaluation(ResultSet rs) throws SQLException {
        SelfEvaluation eval = new SelfEvaluation();
        eval.setSelfEvalId(rs.getInt("self_eval_id"));
        eval.setQ1(rs.getInt("q1"));
        eval.setQ2(rs.getInt("q2"));
        eval.setQ3(rs.getInt("q3"));
        eval.setQ4(rs.getInt("q4"));
        eval.setQ5(rs.getInt("q5"));
        eval.setQ6(rs.getInt("q6"));
        eval.setQ7(rs.getInt("q7"));
        eval.setQ8(rs.getInt("q8"));
        eval.setQ9(rs.getInt("q9"));
        eval.setQ10(rs.getInt("q10"));
        eval.setEvidence(rs.getString("evidence"));
        eval.setPractitionerId(rs.getInt("practitioner_id"));
        eval.setReportId(rs.getInt("report_id"));
        eval.setStatus(rs.getString("status"));
        return eval;
    }
}