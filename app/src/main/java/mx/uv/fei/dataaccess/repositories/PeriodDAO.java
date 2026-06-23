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
    private static final String SQL_SELECT_PERIOD_BY_ID =
            "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period WHERE period_id = ?";
    private static final String SQL_UPDATE_PERIOD =
            "UPDATE school_period SET period_name = ?, start_date = ?, end_date = ? WHERE period_id = ?";
    private static final String SQL_ACTIVATE_PERIOD =
            "UPDATE school_period SET period_status = 'Active' WHERE period_id = ?";
    private static final String SQL_DEACTIVATE_PERIOD =
            "UPDATE school_period SET period_status = 'Concluded' WHERE period_id = ?";

    @Inject
    public PeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

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

    @Override
    public List<Period> getAllPeriods() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_PERIODS, this::mapResultSetToPeriod);
    }

    @Override
    public Period recoverPeriod(int periodId) throws DAOException {
        List<Period> periods = recoverALL(SQL_SELECT_PERIOD_BY_ID, this::mapResultSetToPeriod, periodId);
        return periods.isEmpty() ? new Period() : periods.getFirst();
    }

    @Override
    public void updatePeriod(Period period, int periodId) throws DAOException {
        updateTuple(SQL_UPDATE_PERIOD, statement -> {
            statement.setString(1, period.getPeriodName());
            statement.setDate(2, period.getStartDate());
            statement.setDate(3, period.getEndDate());
            statement.setInt(4, periodId);
        });
    }

    @Override
    public void activatePeriod(int periodId) throws DAOException {
        updateTuple(SQL_ACTIVATE_PERIOD, statement -> statement.setInt(1, periodId));
    }

    @Override
    public void deactivatePeriod(int periodId) throws DAOException {
        updateTuple(SQL_DEACTIVATE_PERIOD, statement -> statement.setInt(1, periodId));
    }

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