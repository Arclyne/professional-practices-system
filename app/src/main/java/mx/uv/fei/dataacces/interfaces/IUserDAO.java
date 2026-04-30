package mx.uv.fei.dataacces.interfaces;

import java.sql.Connection;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.User;

public interface IUserDAO {
    int insertUser(User user, Connection sharedConnection) throws DAOException;

    boolean deactivateUser(int idUsuario) throws DAOException;

    boolean updateUser(User user, Connection sharedConnection) throws DAOException;

    boolean verifyCredentials(String userName, String password) throws DAOException;

    String getUserRole(String userName) throws DAOException;

    User getUserByUserName(String userName) throws DAOException;
}