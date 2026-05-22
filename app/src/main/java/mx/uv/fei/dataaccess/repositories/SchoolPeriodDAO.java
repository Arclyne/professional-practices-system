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
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.ISchoolPeriodDAO;

@Component
public class SchoolPeriodDAO extends BaseDAO implements ISchoolPeriodDAO {

    private static final String SQL_INSERT = "INSERT INTO school_period (period_name, start_date, end_date, period_status) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period WHERE period_id = ?";
    private static final String SQL_SELECT_ALL = "SELECT period_id, period_name, start_date, end_date, period_status FROM school_period";
    private static final String SQL_UPDATE = "UPDATE school_period SET period_name = ?, start_date = ?, end_date = ?, period_status = ? WHERE period_id = ?";

    @Inject
    public SchoolPeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertSchoolPeriod(SchoolPeriod period) throws DAOException {
        int generatedId = -1;
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, period.getPeriodName());
            statement.setDate(2, period.getStartDate() != null ? java.sql.Date.valueOf(period.getStartDate()) : null);
            statement.setDate(3, period.getEndDate() != null ? java.sql.Date.valueOf(period.getEndDate()) : null);
            statement.setString(4, period.getStatus());
            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error saving the school period to the database.", e);
        }
        return generatedId;
    }

    @Override
    public SchoolPeriod recoverSchoolPeriod(int periodId) throws DAOException {
        SchoolPeriod periodToSearch = new SchoolPeriod();
        try (Connection connection = databaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {
            statement.setInt(1, periodId);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    periodToSearch.setPeriodId(resultSet.getInt("period_id"));
                    periodToSearch.setPeriodName(resultSet.getString("period_name"));
                    if (resultSet.getDate("start_date") != null) {
                        periodToSearch.setStartDate(resultSet.getDate("start_date").toLocalDate());
                    }
                    if (resultSet.getDate("end_date") != null) {
                        periodToSearch.setEndDate(resultSet.getDate("end_date").toLocalDate());
                    }
                    periodToSearch.setStatus(resultSet.getString("period_status"));
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error recovering the school period from the database.", e);
        }
        return periodToSearch;
    }

    @Override
    public List<SchoolPeriod> getAllSchoolPeriods() throws DAOException {
        return recoverALL(SQL_SELECT_ALL, resultSet -> {
            SchoolPeriod periodRecovered = new SchoolPeriod();
            periodRecovered.setPeriodId(resultSet.getInt("period_id"));
            periodRecovered.setPeriodName(resultSet.getString("period_name"));
            if (resultSet.getDate("start_date") != null) {
                periodRecovered.setStartDate(resultSet.getDate("start_date").toLocalDate());
            }
            if (resultSet.getDate("end_date") != null) {
                periodRecovered.setEndDate(resultSet.getDate("end_date").toLocalDate());
            }
            periodRecovered.setStatus(resultSet.getString("period_status"));
            return periodRecovered;
        });
    }

    @Override
    public boolean updateSchoolPeriod(SchoolPeriod period, int id) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, period.getPeriodName());
            statement.setDate(2, period.getStartDate() != null ? java.sql.Date.valueOf(period.getStartDate()) : null);
            statement.setDate(3, period.getEndDate() != null ? java.sql.Date.valueOf(period.getEndDate()) : null);
            statement.setString(4, period.getStatus());
            statement.setInt(5, id);
        });
    }
}