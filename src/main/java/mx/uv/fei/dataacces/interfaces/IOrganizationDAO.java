package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.dataacces.exceptions.DAOException;

public interface IOrganizationDAO {
    boolean insertOrganization(Organization organizacion) throws DAOException;
}
