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
import mx.uv.fei.dataaccess.interfaces.IPeriodDAO;
import mx.uv.fei.domain.dto.Period;

@Component
public class PeriodDAO extends BaseDAO implements IPeriodDAO {

    private static final String SQL_INSERT = "INSERT INTO school_period (period_name, start_date, end_date, period_status) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ALL = "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period";

    private static final String MSG_INSERT_ERROR = "Database error while attempting to save the academic period.";
    private static final String MSG_RECOVER_ERROR = "Database error while recovering academic periods.";

    @Inject
    public PeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertPeriod(Period period) throws DAOException {
        int generatedId = -1;

        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {

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
        } catch (SQLException exception) {
            throw new DAOException(MSG_INSERT_ERROR, exception);
        }

        return generatedId;
    }

    @Override
    public List<Period> getAllPeriods() throws DAOException {
        try {
            return recoverALL(SQL_SELECT_ALL, resultSet -> {
                Period recoveredPeriod = new Period();
                recoveredPeriod.setPeriodId(resultSet.getInt("period_id"));
                recoveredPeriod.setPeriodName(resultSet.getString("period_name"));
                recoveredPeriod.setStartDate(resultSet.getDate("start_date"));
                recoveredPeriod.setEndDate(resultSet.getDate("end_date"));
                recoveredPeriod.setPeriodStatus(resultSet.getString("period_status"));
                return recoveredPeriod;
            });
        } catch (DAOException exception) {
            throw new DAOException(MSG_RECOVER_ERROR, exception);
        }
    }
}