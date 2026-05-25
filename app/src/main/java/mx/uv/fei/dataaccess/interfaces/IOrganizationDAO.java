package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.domain.dto.Organization;

import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;

public interface IOrganizationDAO {
    boolean insertOrganization(Organization organization) throws DAOException;

    Organization recoverOrganization(String organizationName) throws DAOException;

    List<Organization> getAllOrganizations() throws DAOException;

    boolean updateOrganization(Organization updateOrganization, int ID) throws DAOException;

    boolean deactivateMultipleOrganizations(List<Integer> organizationIdentifiersList) throws DAOException;
}
