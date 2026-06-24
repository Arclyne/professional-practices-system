package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.dto.PractitionerGrade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.List;

/**
 * Acceso a datos de las calificaciones de los practicantes.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class PractitionerGradeDAO extends BaseDAO implements IPractitionerGradeDAO {

    private static final String SQL_INSERT_PRACTITIONER_GRADE =
            "INSERT INTO practitioner_grade (practitioner_id, professor_id, tentative_grade, " +
                    "final_grade, period) VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_UPDATE_FINAL_GRADE =
            "UPDATE practitioner_grade SET final_grade = ?, graded_at = NOW() WHERE grade_id = ?";
    private static final String SQL_SELECT_GRADE_BY_PRACTITIONER_AND_PERIOD =
            "SELECT * FROM practitioner_grade WHERE practitioner_id = ? AND period = ?";
    private static final String SQL_SELECT_GRADES_BY_PROFESSOR =
            "SELECT * FROM practitioner_grade WHERE professor_id = ? ORDER BY graded_at DESC";
    private static final String SQL_CALCULATE_TENTATIVE_GRADE =
            "SELECT COALESCE(AVG(grade), 0.0) FROM monthly_report " +
                    "WHERE practitioner_id = ? AND status = 'Evaluado' AND grade IS NOT NULL";

    @Inject
    public PractitionerGradeDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertPractitionerGrade(PractitionerGrade practitionerGrade) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PRACTITIONER_GRADE, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, practitionerGrade.getPractitionerId());
            statement.setInt(2, practitionerGrade.getProfessorId());
            statement.setDouble(3, practitionerGrade.getTentativeGrade());
            statement.setObject(4, practitionerGrade.getFinalGrade(), Types.DECIMAL);
            statement.setString(5, practitionerGrade.getPeriod());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al registrar la calificación del practicante.", e);
        }

        return generatedId;
    }

    @Override
    public void updateFinalGrade(int gradeId, double finalGrade) throws DAOException {
        updateTuple(SQL_UPDATE_FINAL_GRADE, statement -> {
            statement.setDouble(1, finalGrade);
            statement.setInt(2, gradeId);
        });
    }

    @Override
    public PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws DAOException {
        PractitionerGrade recoveredGrade = null;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_GRADE_BY_PRACTITIONER_AND_PERIOD)) {

            statement.setInt(1, practitionerId);
            statement.setString(2, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredGrade = mapResultSetToPractitionerGrade(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar la calificación del practicante.", e);
        }

        return recoveredGrade;
    }

    @Override
    public List<PractitionerGrade> getGradesByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_GRADES_BY_PROFESSOR, this::mapResultSetToPractitionerGrade, professorId);
    }

    @Override
    public double calculateTentativeGrade(int practitionerId) throws DAOException {
        double tentativeGrade = 0.0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CALCULATE_TENTATIVE_GRADE)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    tentativeGrade = resultSet.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al calcular la calificación tentativa.", e);
        }

        return tentativeGrade;
    }

    private PractitionerGrade mapResultSetToPractitionerGrade(ResultSet resultSet) throws SQLException {
        PractitionerGrade practitionerGrade = new PractitionerGrade();
        practitionerGrade.setGradeId(resultSet.getInt("grade_id"));
        practitionerGrade.setPractitionerId(resultSet.getInt("practitioner_id"));
        practitionerGrade.setProfessorId(resultSet.getInt("professor_id"));
        practitionerGrade.setTentativeGrade(resultSet.getDouble("tentative_grade"));
        practitionerGrade.setFinalGrade(resolveNullableFinalGrade(resultSet));
        practitionerGrade.setPeriod(resultSet.getString("period"));
        practitionerGrade.setGradedAt(resolveNullableTimestamp(resultSet, "graded_at"));
        return practitionerGrade;
    }

    private Double resolveNullableFinalGrade(ResultSet resultSet) throws SQLException {
        double finalGrade = resultSet.getDouble("final_grade");
        return resultSet.wasNull() ? null : finalGrade;
    }

    private java.time.LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        java.sql.Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}