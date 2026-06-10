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

    @Inject
    public SchoolPeriodDAO(IDatabaseConnection databaseConnection) {
        super(databaseConnection);
    }

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

    @Override
    public List<SchoolPeriod> getAllSchoolPeriods() throws DAOException {
        return recoverALL(SQL_SELECT_ALL_SCHOOL_PERIODS, this::mapResultSetToSchoolPeriod);
    }

    @Override
    public boolean updateSchoolPeriod(SchoolPeriod schoolPeriod, int periodId) throws DAOException {
        return updateTuple(SQL_UPDATE_SCHOOL_PERIOD, statement -> {
            statement.setString(1, schoolPeriod.getPeriodName());
            statement.setDate(2, toSqlDate(schoolPeriod.getStartDate()));
            statement.setDate(3, toSqlDate(schoolPeriod.getEndDate()));
            statement.setString(4, schoolPeriod.getStatus());
            statement.setInt(5, periodId);
        });
    }

    private SchoolPeriod mapResultSetToSchoolPeriod(ResultSet resultSet) throws SQLException {
        SchoolPeriod schoolPeriod = new SchoolPeriod();
        schoolPeriod.setPeriodId(resultSet.getInt("period_id"));
        schoolPeriod.setPeriodName(resultSet.getString("period_name"));
        schoolPeriod.setStartDate(toLocalDate(resultSet.getDate("start_date")));
        schoolPeriod.setEndDate(toLocalDate(resultSet.getDate("end_date")));
        schoolPeriod.setStatus(resultSet.getString("period_status"));
        return schoolPeriod;
    }

    private LocalDate toLocalDate(Date sqlDate) {
        return sqlDate != null ? sqlDate.toLocalDate() : null;
    }

    private Date toSqlDate(LocalDate localDate) {
        return localDate != null ? Date.valueOf(localDate) : null;
    }
}