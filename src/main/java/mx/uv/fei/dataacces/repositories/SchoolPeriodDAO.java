package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.interfaces.ISchoolPeriodDAO;

public class SchoolPeriodDAO extends BaseDAO implements ISchoolPeriodDAO {

    private static final String SQL_INSERT = "INSERT INTO PERIODO_ESCOLAR (NOMBRE_PERIODO, FECHA_INICIO, FECHA_FIN, ESTADO_PERIODO) VALUES (?, ?, ?, ?)";
    private static final String SQL_SELECT_ONE = "SELECT ID_PERIODO, NOMBRE_PERIODO, FECHA_INICIO, FECHA_FIN, ESTADO_PERIODO FROM PERIODO_ESCOLAR WHERE ID_PERIODO = ?";
    private static final String SQL_SELECT_ALL = "SELECT ID_PERIODO, NOMBRE_PERIODO, FECHA_INICIO, FECHA_FIN, ESTADO_PERIODO FROM PERIODO_ESCOLAR";
    private static final String SQL_UPDATE = "UPDATE PERIODO_ESCOLAR SET NOMBRE_PERIODO = ?, FECHA_INICIO = ?, FECHA_FIN = ?, ESTADO_PERIODO = ? WHERE ID_PERIODO = ?";

    public SchoolPeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

    @Override
    public int insertSchoolPeriod(SchoolPeriod period) throws DAOException {
        int generatedId = -1;

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_INSERT,
                        Statement.RETURN_GENERATED_KEYS)) {

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

        try (
                Connection connection = databaseConnection.getConnection();
                PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ONE)) {

            statement.setInt(1, periodId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    periodToSearch.setPeriodId(resultSet.getInt("ID_PERIODO"));
                    periodToSearch.setPeriodName(resultSet.getString("NOMBRE_PERIODO"));

                    if (resultSet.getDate("FECHA_INICIO") != null) {
                        periodToSearch.setStartDate(resultSet.getDate("FECHA_INICIO").toLocalDate());
                    }
                    if (resultSet.getDate("FECHA_FIN") != null) {
                        periodToSearch.setEndDate(resultSet.getDate("FECHA_FIN").toLocalDate());
                    }

                    periodToSearch.setStatus(resultSet.getString("ESTADO_PERIODO"));
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

            periodRecovered.setPeriodId(resultSet.getInt("ID_PERIODO"));
            periodRecovered.setPeriodName(resultSet.getString("NOMBRE_PERIODO"));

            if (resultSet.getDate("FECHA_INICIO") != null) {
                periodRecovered.setStartDate(resultSet.getDate("FECHA_INICIO").toLocalDate());
            }
            if (resultSet.getDate("FECHA_FIN") != null) {
                periodRecovered.setEndDate(resultSet.getDate("FECHA_FIN").toLocalDate());
            }

            periodRecovered.setStatus(resultSet.getString("ESTADO_PERIODO"));

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