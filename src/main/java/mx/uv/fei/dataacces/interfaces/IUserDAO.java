package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.exceptions.DAOException;

public interface IUserDAO {
    int insertUser(User user) throws DAOException;
}