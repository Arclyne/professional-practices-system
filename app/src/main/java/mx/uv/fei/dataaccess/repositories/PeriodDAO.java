package mx.uv.fei.dataaccess.repositories;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.domain.dto.Period;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

/**
 * Acceso a datos de los periodos escolares.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @author José Eduardo Prior Hernández
 * @version 1.0
 */
@Component
public class PeriodDAO extends BaseDAO implements IPeriodDAO {

    private static final String SQL_INSERT_PERIOD =
            "INSERT INTO school_period (period_name, start_date, end_date, period_status) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL_PERIODS =
            "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period";
    private static final String SQL_SELECT_ACTIVE_PERIOD =
            "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period " +
                    "WHERE period_status = 'Active' ORDER BY start_date DESC";
    private static final String SQL_SELECT_PERIOD_BY_ID =
            "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period WHERE period_id = ?";
    private static final String SQL_UPDATE_PERIOD =
            "UPDATE school_period SET period_name = ?, start_date = ?, end_date = ? WHERE period_id = ?";
    private static final String SQL_ACTIVATE_PERIOD =
            "UPDATE school_period SET period_status = 'Active' WHERE period_id = ?";
    private static final String SQL_DEACTIVATE_PERIOD =
            "UPDATE school_period SET period_status = 'Concluded' WHERE period_id = ?";

    /**
     * Crea el DAO de periodos escolares con la fuente de conexiones a la base de datos.
     *
     * @param databaseConnection proveedor de conexiones a la base de datos
     */
    @Inject
    public PeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    /**
     * Inserta un nuevo periodo escolar y devuelve su identificador generado.
     *
     * @param period periodo con los datos a registrar
     * @return identificador generado para el periodo, o {@code -1} si no se generó
     * @throws DAOException si ocurre un error al guardar el periodo
     */
    @Override
    public int insertPeriod(Period period) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT_PERIOD, Statement.RETURN_GENERATED_KEYS)) {

            statement.setString(1, period.getPeriodName());
            statement.setDate(2, period.getStartDate());
            statement.setDate(3, period.getEndDate());
            statement.setString(4, period.getPeriodStatus());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error en la base de datos al intentar guardar el periodo académico.", e);
        }

        return generatedId;
    }

    /**
     * Recupera todos los periodos escolares registrados.
     *
     * @return lista con todos los periodos; vacía si no hay registros
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public List<Period> getAllPeriods() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PERIODS, this::mapResultSetToPeriod);
    }

    /**
     * Recupera el periodo escolar activo más reciente.
     *
     * @return periodo activo más reciente, o {@code null} si no hay ninguno activo
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Period getActivePeriod() throws DAOException {
        List<Period> activePeriods = recoverALL(SQL_SELECT_ACTIVE_PERIOD, this::mapResultSetToPeriod);
        return activePeriods.isEmpty() ? null : activePeriods.getFirst();
    }

    /**
     * Recupera un periodo escolar a partir de su identificador.
     *
     * @param periodId identificador del periodo a recuperar
     * @return periodo encontrado, o un {@link Period} vacío si no existe
     * @throws DAOException si ocurre un error al consultar la base de datos
     */
    @Override
    public Period recoverPeriod(int periodId) throws DAOException {
        List<Period> periods = recoverALL(SQL_SELECT_PERIOD_BY_ID, this::mapResultSetToPeriod, periodId);
        return periods.isEmpty() ? new Period() : periods.getFirst();
    }

    /**
     * Actualiza el nombre y las fechas de un periodo escolar.
     *
     * @param period   periodo con los datos modificados
     * @param periodId identificador del periodo a actualizar
     * @throws DAOException si el periodo no existe o si ocurre un error al actualizar
     */
    @Override
    public void updatePeriod(Period period, int periodId) throws DAOException {
        updateTuple(SQL_UPDATE_PERIOD, statement -> {
            statement.setString(1, period.getPeriodName());
            statement.setDate(2, period.getStartDate());
            statement.setDate(3, period.getEndDate());
            statement.setInt(4, periodId);
        });
    }

    /**
     * Marca un periodo escolar como activo.
     *
     * @param periodId identificador del periodo a activar
     * @throws DAOException si el periodo no existe o si ocurre un error al actualizar
     */
    @Override
    public void activatePeriod(int periodId) throws DAOException {
        updateTuple(SQL_ACTIVATE_PERIOD, statement -> statement.setInt(1, periodId));
    }

    /**
     * Marca un periodo escolar como concluido.
     *
     * @param periodId identificador del periodo a concluir
     * @throws DAOException si el periodo no existe o si ocurre un error al actualizar
     */
    @Override
    public void deactivatePeriod(int periodId) throws DAOException {
        updateTuple(SQL_DEACTIVATE_PERIOD, statement -> statement.setInt(1, periodId));
    }

    /**
     * Construye un periodo con los valores de la fila actual del resultado.
     *
     * @param resultSet resultado posicionado en la fila a mapear
     * @return periodo con los datos de la fila
     * @throws SQLException si ocurre un error al leer alguna columna
     */
    private Period mapResultSetToPeriod(ResultSet resultSet) throws SQLException {
        Period period = new Period();
        period.setPeriodId(resultSet.getInt("period_id"));
        period.setPeriodName(resultSet.getString("period_name"));
        period.setStartDate(resultSet.getDate("start_date"));
        period.setEndDate(resultSet.getDate("end_date"));
        period.setPeriodStatus(resultSet.getString("period_status"));
        return period;
    }
}