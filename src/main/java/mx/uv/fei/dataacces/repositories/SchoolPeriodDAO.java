package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.SchoolPeriod;
import mx.uv.fei.dataacces.interfaces.ISchoolPeriodDAO;

public class SchoolPeriodDAO implements ISchoolPeriodDAO {

    public int insertSchoolPeriod(SchoolPeriod period) throws DAOException {
        int generatedId = -1;
        String query = "INSERT INTO PERIODO_ESCOLAR (NOMBRE_PERIODO, FECHA_INICIO, FECHA_FIN, ESTADO_PERIODO) VALUES (?, ?, ?, ?)";

        try (
            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
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
}