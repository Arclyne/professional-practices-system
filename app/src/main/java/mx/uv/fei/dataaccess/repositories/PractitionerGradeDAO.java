package mx.uv.fei.dataaccess.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerGradeDAO;
import mx.uv.fei.domain.dto.PractitionerGrade;

@Component
public class PractitionerGradeDAO extends BaseDAO implements IPractitionerGradeDAO {

    private static final String SQL_INSERT =
            "INSERT INTO practitioner_grade (practitioner_id, professor_id, tentative_grade, " +
            "final_grade, period) VALUES (?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE_FINAL_GRADE =
            "UPDATE practitioner_grade SET final_grade = ?, graded_at = NOW() WHERE grade_id = ?";

    private static final String SQL_SELECT_BY_PRACTITIONER_AND_PERIOD =
            "SELECT * FROM practitioner_grade WHERE practitioner_id = ? AND period = ?";

    private static final String SQL_SELECT_BY_PROFESSOR =
            "SELECT * FROM practitioner_grade WHERE professor_id = ? ORDER BY graded_at DESC";

    private static final String SQL_CALCULATE_TENTATIVE =
            "SELECT COALESCE(AVG(grade), 0.0) FROM monthly_report " +
            "WHERE practitioner_id = ? AND status = 'Evaluado' AND grade IS NOT NULL";

    private static final String MSG_INSERT_ERROR  = "Error al registrar la calificación del practicante.";
    private static final String MSG_UPDATE_ERROR  = "Error al actualizar la calificación final.";
    private static final String MSG_SELECT_ERROR  = "Error al recuperar la calificación del practicante.";
    private static final String MSG_CALC_ERROR    = "Error al calcular la calificación tentativa.";

    @Inject
    public PractitionerGradeDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertPractitionerGrade(PractitionerGrade grade) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

            statement.setInt(1, grade.getPractitionerId());
            statement.setInt(2, grade.getProfessorId());
            statement.setDouble(3, grade.getTentativeGrade());

            if (grade.getFinalGrade() != null) {
                statement.setDouble(4, grade.getFinalGrade());
            } else {
                statement.setNull(4, java.sql.Types.DECIMAL);
            }

            statement.setString(5, grade.getPeriod());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_INSERT_ERROR, e);
        }

        return generatedId;
    }

    @Override
    public boolean updateFinalGrade(int gradeId, double finalGrade) throws DAOException {
        return updateTuple(SQL_UPDATE_FINAL_GRADE, statement -> {
            statement.setDouble(1, finalGrade);
            statement.setInt(2, gradeId);
        });
    }

    @Override
    public PractitionerGrade getGradeByPractitionerAndPeriod(int practitionerId, String period) throws DAOException {
        PractitionerGrade recoveredGrade = new PractitionerGrade();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_PRACTITIONER_AND_PERIOD)) {

            statement.setInt(1, practitionerId);
            statement.setString(2, period);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredGrade = mapResultSetToGrade(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_SELECT_ERROR, e);
        }

        return recoveredGrade;
    }

    @Override
    public List<PractitionerGrade> getGradesByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_BY_PROFESSOR, this::mapResultSetToGrade, professorId);
    }

    @Override
    public double calculateTentativeGrade(int practitionerId) throws DAOException {
        double tentativeGrade = 0.0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CALCULATE_TENTATIVE)) {

            statement.setInt(1, practitionerId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    tentativeGrade = resultSet.getDouble(1);
                }
            }
        } catch (SQLException e) {
            throw new DAOException(MSG_CALC_ERROR, e);
        }

        return tentativeGrade;
    }

    private PractitionerGrade mapResultSetToGrade(ResultSet resultSet) throws SQLException {
        PractitionerGrade grade = new PractitionerGrade();

        grade.setGradeId(resultSet.getInt("grade_id"));
        grade.setPractitionerId(resultSet.getInt("practitioner_id"));
        grade.setProfessorId(resultSet.getInt("professor_id"));
        grade.setTentativeGrade(resultSet.getDouble("tentative_grade"));

        double finalGradeValue = resultSet.getDouble("final_grade");
        grade.setFinalGrade(resultSet.wasNull() ? null : finalGradeValue);

        grade.setPeriod(resultSet.getString("period"));

        if (resultSet.getTimestamp("graded_at") != null) {
            grade.setGradedAt(resultSet.getTimestamp("graded_at").toLocalDateTime());
        }

        return grade;
    }
}
