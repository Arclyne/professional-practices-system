package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.User;

public interface IUserDAO {
    int insertUser(User user) throws DAOException;
}