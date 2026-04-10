package mx.uv.fei.dataacces.repositories;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IPracticeGroupDAO;
import mx.uv.fei.domain.dto.PracticeGroup;

public class PracticeGroupDAO implements IPracticeGroupDAO {

    public int insertPracticeGroup(PracticeGroup group) throws DAOException {
        int generatedIndex = -1;
        String query = "INSERT INTO GRUPO_PRACTICAS (SECCION, ID_PROFESOR, ID_PERIODO) VALUES (?, ?, ?)";

        try (
            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, group.getSection());
            statement.setInt(2, group.getProfessorId());
            statement.setInt(3, group.getPeriodId());

            if (statement.executeUpdate() > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        generatedIndex = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error saving the practice group to the database. Ensure Professor ID and Period ID exist.", e);
        }

        return generatedIndex;
    }
}