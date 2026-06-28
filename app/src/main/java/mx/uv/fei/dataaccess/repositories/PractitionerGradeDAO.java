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
            "SELECT COALESCE(AVG(grade), 0.0) FROM (" +
                    "SELECT grade FROM monthly_report " +
                    "WHERE practitioner_id = ? AND status = 'Evaluado' AND grade IS NOT NULL " +
                    "UNION ALL " +
                    "SELECT grade FROM progress_report " +
                    "WHERE practitioner_id = ? AND status = 'Evaluado' AND grade IS NOT NULL" +
                    ") AS evaluated_grades";

    /**
     * Crea el DAO de calificaciones de practicantes con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public PractitionerGradeDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Registra una calificación de practicante y devuelve su identificador generado.
     *
     * @param practitionerGrade calificación con los datos a registrar
     * @return identificador generado para la calificación, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar la calificación
     */
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

    /**
     * Asigna la calificación final a un registro y marca la fecha de calificación.
     *
     * @param gradeId    identificador de la calificación a actualizar
     * @param finalGrade calificación final a registrar
     * @throws DAOException si el registro no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateFinalGrade(int gradeId, double finalGrade) throws DAOException {
        updateTuple(SQL_UPDATE_FINAL_GRADE, statement -> {
            statement.setDouble(1, finalGrade);
            statement.setInt(2, gradeId);
        });
    }

    /**
     * Recupera la calificación de un practicante en un periodo determinado.
     *
     * @param practitionerId identificador del practicante
     * @param period         periodo al que corresponde la calificación
     * @return calificación encontrada, o {@code null} si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
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

    /**
     * Recupera todas las calificaciones registradas por un profesor, de la más reciente a la más antigua.
     *
     * @param professorId identificador del profesor
     * @return lista de calificaciones del profesor; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<PractitionerGrade> getGradesByProfessor(int professorId) throws DAOException {
        return recoverALL(SQL_SELECT_GRADES_BY_PROFESSOR, this::mapResultSetToPractitionerGrade, professorId);
    }

    /**
     * Calcula la calificación tentativa de un practicante como promedio de sus reportes evaluados.
     *
     * @param practitionerId identificador del practicante
     * @return promedio de las calificaciones evaluadas, o {@code 0.0} si no hay ninguna
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public double calculateTentativeGrade(int practitionerId) throws DAOException {
        double tentativeGrade = 0.0;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_CALCULATE_TENTATIVE_GRADE)) {

            statement.setInt(1, practitionerId);
            statement.setInt(2, practitionerId);

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

    /**
     * Construye una calificación con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return calificación con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
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

    /**
     * Obtiene la calificación final tolerando valores nulos en la columna.
     *
     * @param resultSet resultado posicionado en la fila a leer
     * @return calificación final, o {@code null} si aún no ha sido asignada
     * @throws SQLException si ocurre un error al leer la columna
     */
    private Double resolveNullableFinalGrade(ResultSet resultSet) throws SQLException {
        double finalGrade = resultSet.getDouble("final_grade");
        return resultSet.wasNull() ? null : finalGrade;
    }

    /**
     * Obtiene una marca de tiempo como {@link java.time.LocalDateTime} tolerando valores nulos.
     *
     * @param resultSet  resultado posicionado en la fila a leer
     * @param columnName nombre de la columna de tipo marca de tiempo
     * @return fecha y hora correspondiente, o {@code null} si la columna es nula
     * @throws SQLException si ocurre un error al leer la columna
     */
    private java.time.LocalDateTime resolveNullableTimestamp(ResultSet resultSet, String columnName) throws SQLException {
        java.sql.Timestamp timestamp = resultSet.getTimestamp(columnName);
        return timestamp != null ? timestamp.toLocalDateTime() : null;
    }
}