package mx.uv.fei.dataaccess.interfaces;

import java.time.LocalDateTime;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.AuthenticationToken;

public interface IAuthenticationToken {
    void insertToken(AuthenticationToken token) throws DAOException;

    AuthenticationToken recoverToken(int tokenValue) throws DAOException;

    LocalDateTime getTokenCreationTime(int tokenValue, String userName) throws DAOException;
}
