package mx.uv.fei.dataacces.interfaces;

import java.time.LocalDateTime;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.AuthenticationToken;

public interface IAuthenticationToken {
    boolean insertToken(AuthenticationToken token) throws DAOException;

    AuthenticationToken recoverToken(int tokenValue) throws DAOException;

    LocalDateTime getTokenCreationTime(int tokenValue, String userName) throws DAOException;
}
