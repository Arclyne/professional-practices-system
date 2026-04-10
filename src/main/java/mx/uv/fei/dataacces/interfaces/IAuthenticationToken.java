package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.AuthenticationToken;

public interface IAuthenticationToken {
    public boolean insterToken(AuthenticationToken token) throws DAOException;
}
