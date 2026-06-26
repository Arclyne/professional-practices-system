package mx.uv.fei.dataaccess.interfaces;

import java.sql.Connection;
import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.User;

public interface IUserDAO {
    int insertUser(User user, Connection sharedConnection) throws DAOException;
    void deactivateUser(int idUsuario) throws DAOException;
    void activateUser(int userId) throws DAOException;
    void updateUser(User user, Connection sharedConnection) throws DAOException;
    String getUserRole(String userName) throws DAOException;
    User getUserByUserName(String userName) throws DAOException;
    User getUserByEmail(String email) throws DAOException;
    void deactivateMultipleUsers(List<Integer> userIdentifiersList) throws DAOException;
}
