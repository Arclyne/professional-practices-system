package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISchoolPeriodDAO;
import mx.uv.fei.domain.dto.SchoolPeriod;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

/**
 * Acceso a datos de los periodos escolares y su actualización.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class SchoolPeriodDAO extends BaseDAO implements ISchoolPeriodDAO {

    private static final String SQL_INSERT_SCHOOL_PERIOD =
            "INSERT INTO school_period (period_name, start_date, end_date, period_status) VALUES (?, ?, ?, ?)";
    private static final String SQL_UPDATE_SCHOOL_PERIOD =
            "UPDATE school_period SET period_name = ?, start_date = ?, end_date = ?, period_status = ? WHERE period_id = ?";
    private static final String SQL_SELECT_SCHOOL_PERIOD_BY_ID =
            "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period WHERE period_id = ?";
    private static final String SQL_SELECT_ALL_SCHOOL_PERIODS =
            "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period";

    /**
     * Crea el DAO de periodos escolares con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public SchoolPeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un nuevo periodo escolar y devuelve su identificador generado.
     *
     * @param schoolPeriod periodo escolar con los datos a registrar
     * @return identificador generado para el periodo, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el periodo
     */
    @Override
    public int insertSchoolPeriod(SchoolPeriod schoolPeriod) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_SCHOOL_PERIOD, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, schoolPeriod.getPeriodName());
            statement.setDate(2, toSqlDate(schoolPeriod.getStartDate()));
            statement.setDate(3, toSqlDate(schoolPeriod.getEndDate()));
            statement.setString(4, schoolPeriod.getStatus());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al guardar el periodo escolar en la base de datos.", e);
        }

        return generatedId;
    }

    /**
     * Recupera un periodo escolar a partir de su identificador.
     *
     * @param periodId identificador del periodo a recuperar
     * @return periodo encontrado, o un {@link SchoolPeriod} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public SchoolPeriod recoverSchoolPeriod(int periodId) throws DAOException {
        SchoolPeriod recoveredPeriod = new SchoolPeriod();

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_SCHOOL_PERIOD_BY_ID)) {

            statement.setInt(1, periodId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    recoveredPeriod = mapResultSetToSchoolPeriod(resultSet);
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al recuperar el periodo escolar de la base de datos.", e);
        }

        return recoveredPeriod;
    }

    /**
     * Recupera todos los periodos escolares registrados.
     *
     * @return lista con todos los periodos escolares; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<SchoolPeriod> getAllSchoolPeriods() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_SCHOOL_PERIODS, this::mapResultSetToSchoolPeriod);
    }

    /**
     * Actualiza los datos de un periodo escolar existente.
     *
     * @param schoolPeriod periodo escolar con los datos modificados
     * @param periodId     identificador del periodo a actualizar
     * @throws DAOException si el periodo no existe o si ocurre un error al actualizar
     */
    @Override
    public void updateSchoolPeriod(SchoolPeriod schoolPeriod, int periodId) throws DAOException {
        updateTuple(SQL_UPDATE_SCHOOL_PERIOD, statement -> {
            statement.setString(1, schoolPeriod.getPeriodName());
            statement.setDate(2, toSqlDate(schoolPeriod.getStartDate()));
            statement.setDate(3, toSqlDate(schoolPeriod.getEndDate()));
            statement.setString(4, schoolPeriod.getStatus());
            statement.setInt(5, periodId);
        });
    }

    /**
     * Construye un periodo escolar con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return periodo escolar con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private SchoolPeriod mapResultSetToSchoolPeriod(ResultSet resultSet) throws SQLException {
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        schoolPeriod.setPeriodId(resultSet.getInt("period_id"));
        schoolPeriod.setPeriodName(resultSet.getString("period_name"));
        schoolPeriod.setStartDate(toLocalDate(resultSet.getDate("start_date")));
        schoolPeriod.setEndDate(toLocalDate(resultSet.getDate("end_date")));
        schoolPeriod.setStatus(resultSet.getString("period_status"));
        return schoolPeriod;
    }

    /**
     * Convierte una fecha SQL a {@link LocalDate} tolerando valores nulos.
     *
     * @param sqlDate fecha de tipo {@link Date} a convertir
     * @return fecha como {@link LocalDate}, o {@code null} si la entrada es nula
     */
    private LocalDate toLocalDate(Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }

    /**
     * Convierte un {@link LocalDate} a fecha SQL tolerando valores nulos.
     *
     * @param localDate fecha de tipo {@link LocalDate} a convertir
     * @return fecha como {@link Date}, o {@code null} si la entrada es nula
     */
    private Date toSqlDate(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }
}