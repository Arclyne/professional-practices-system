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

    /**
     * Crea el DAO de autoevaluaciones con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public SelfEvaluationDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Registra una autoevaluación con estado pendiente y devuelve su identificador generado.
     *
     * @param selfEvaluation autoevaluación con los datos a registrar
     * @return identificador generado para la autoevaluación, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar la autoevaluación
     */
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

    /**
     * Recupera la autoevaluación asociada a un reporte.
     *
     * @param reportId identificador del reporte
     * @return autoevaluación encontrada, o {@code null} si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Actualiza el estado y la evidencia de una autoevaluación.
     *
     * @param selfEvaluation   autoevaluación con el estado y la evidencia modificados
     * @param selfEvaluationId identificador de la autoevaluación a actualizar
     * @throws DAOException si la autoevaluación no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateSelfEvaluation(SelfEvaluation selfEvaluation, int selfEvaluationId) throws DAOException {
        updateTuple(SQL_UPDATE_SELF_EVALUATION, statement -> {
            statement.setString(1, selfEvaluation.getStatus());
            statement.setString(2, selfEvaluation.getEvidence());
            statement.setInt(3, selfEvaluationId);
        });
    }

    /**
     * Actualiza únicamente el estado de una autoevaluación.
     *
     * @param selfEvaluationId identificador de la autoevaluación a actualizar
     * @param status           nuevo estado de la autoevaluación
     * @throws DAOException si la autoevaluación no existe o si ocurre un error al actualizar
     */
    public void updateSelfEvaluationStatus(int selfEvaluationId, String status) throws DAOException {
        updateTuple(SQL_UPDATE_SELF_EVALUATION_STATUS, statement -> {
            statement.setString(1, status);
            statement.setInt(2, selfEvaluationId);
        });
    }

    /**
     * Marca una autoevaluación como rechazada y guarda el comentario de revisión.
     *
     * @param selfEvaluationId identificador de la autoevaluación a rechazar
     * @param reviewComment    comentario que explica el motivo del rechazo
     * @throws DAOException si la autoevaluación no existe o si ocurre un error al actualizar
     */
    @Override
    public void rejectSelfEvaluation(int selfEvaluationId, String reviewComment) throws DAOException {
        updateTuple(SQL_REJECT_SELF_EVALUATION, statement -> {
            statement.setString(1, SelfEvaluationStatus.REJECTED.getDatabaseValue());
            statement.setString(2, reviewComment);
            statement.setInt(3, selfEvaluationId);
        });
    }

    /**
     * Actualiza únicamente la evidencia de una autoevaluación.
     *
     * @param selfEvaluationId identificador de la autoevaluación a actualizar
     * @param evidence         nueva evidencia de la autoevaluación
     * @throws DAOException si la autoevaluación no existe o si ocurre un error al actualizar
     */
    public void updateSelfEvaluationEvidence(int selfEvaluationId, String evidence) throws DAOException {
        updateTuple(SQL_UPDATE_SELF_EVALUATION_EVIDENCE, statement -> {
            statement.setString(1, evidence);
            statement.setInt(2, selfEvaluationId);
        });
    }

    /**
     * Enlaza las diez respuestas de la autoevaluación a las primeras posiciones de la sentencia.
     *
     * @param statement      sentencia preparada sobre la que se enlazan las respuestas
     * @param selfEvaluation autoevaluación de la que se obtienen las respuestas
     * @throws SQLException si ocurre un error al enlazar algún parámetro
     */
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

    /**
     * Construye una autoevaluación con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return autoevaluación con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
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