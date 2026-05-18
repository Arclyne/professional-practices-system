package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.ISchoolPeriodDAO;

@Component
public class SchoolPeriodDAO extends BaseDAO implements ISchoolPeriodDAO {

    private static final String SQL_INSERT = "INSERT INTO school_period (PERIOD_NAME, START_DATE, END_DATE, PERIOD_STATUS) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT ID_PERIOD, PERIOD_NAME, START_DATE, END_DATE, PERIOD_STATUS FROM school_period WHERE ID_PERIOD = ?";
    private static final String SQL_SELECT_ALL = "SELECT ID_PERIOD, PERIOD_NAME, START_DATE, END_DATE, PERIOD_STATUS FROM school_period";
    private static final String SQL_UPDATE = "UPDATE school_period SET PERIOD_NAME = ?, START_DATE = ?, END_DATE = ?, PERIOD_STATUS = ? WHERE ID_PERIOD = ?";

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
            statement.setDate(2, java.sql.Date.valueOf(period.getStartDate()));
            statement.setDate(3, java.sql.Date.valueOf(period.getEndDate()));
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
                    periodToSearch.setPeriodId(resultSet.getInt("ID_PERIOD"));
                    periodToSearch.setPeriodName(resultSet.getString("PERIOD_NAME"));
                    if (resultSet.getDate("START_DATE") != null) {
                        periodToSearch.setStartDate(resultSet.getDate("START_DATE").toLocalDate());
                    }
                    if (resultSet.getDate("END_DATE") != null) {
                        periodToSearch.setEndDate(resultSet.getDate("END_DATE").toLocalDate());
                    }
                    periodToSearch.setStatus(resultSet.getString("PERIOD_STATUS"));
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
            periodRecovered.setPeriodId(resultSet.getInt("ID_PERIOD"));
            periodRecovered.setPeriodName(resultSet.getString("PERIOD_NAME"));
            if (resultSet.getDate("START_DATE") != null) {
                periodRecovered.setStartDate(resultSet.getDate("START_DATE").toLocalDate());
            }
            if (resultSet.getDate("END_DATE") != null) {
                periodRecovered.setEndDate(resultSet.getDate("END_DATE").toLocalDate());
            }
            periodRecovered.setStatus(resultSet.getString("PERIOD_STATUS"));
            return periodRecovered;
        });
    }

    @Override
    public boolean updateSchoolPeriod(SchoolPeriod period, int id) throws DAOException {
        return updateTuple(SQL_UPDATE, statement -> {
            statement.setString(1, period.getPeriodName());
            statement.setDate(2, java.sql.Date.valueOf(period.getStartDate()));
            statement.setDate(3, java.sql.Date.valueOf(period.getEndDate()));
            statement.setString(4, period.getStatus());
            statement.setInt(5, id);
        });
    }
}