package mx.uv.fei.dataacces.interfaces;

import java.sql.Connection;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.User;

public interface IUserDAO {
    int insertUser(User user, Connection sharedConnection) throws DAOException;
    boolean deactivateUser(int idUsuario) throws DAOException;
    boolean updateUser(User user, Connection sharedConnection) throws DAOException;
    String getUserRole(String userName) throws DAOException;
    boolean verifyCredentialsByUserName(String userName, String password) throws DAOException;
    boolean verifyCredentialsByEmail(String email, String password) throws DAOException;
    User getUserByUserName(String userName) throws DAOException;
    User getUserByEmail(String email) throws DAOException;

}