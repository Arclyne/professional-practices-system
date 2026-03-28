package mx.uv.fei.dataacces.repositories;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


import mx.uv.fei.dataacces.database.DatabaseConnection;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.dataacces.interfaces.IUserDAO;
import mx.uv.fei.domain.dto.User;


public class UserDAO implements IUserDAO {
    public int insertUser(User user) throws DAOException {
        int generatedId = -1;

        String query = "INSERT INTO USUARIO (PASSWORD, NOMBRE, APELLIDOS, ESTADO, GENERO) VALUES (?, ?, ?, ?)";

        try (
            Connection connection = DatabaseConnection.getInstance().getConnection();
            PreparedStatement statement = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)
        ) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getLastName());
            statement.setString(4, user.getStatus());
            statement.setString(5, user.getGender());

            int affectedRows = statement.executeUpdate();

            if (affectedRows > 0) {
                try (
                        ResultSet generatedKeys = statement.getGeneratedKeys()
                    ) {
                    if (generatedKeys.next()) {
                        generatedId = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DAOException("Error al intentar insertar el usuario en la base de datos.", e);
        }

        return generatedId;
    }
}