package mx.uv.fei.dataaccess.interfaces;

import mx.uv.fei.domain.dto.Organization;

import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;

public interface IOrganizationDAO {
    int insertOrganization(Organization organization) throws DAOException;
    Organization recoverOrganization(String organizationName) throws DAOException;
    List<Organization> getAllOrganizations() throws DAOException;
    void updateOrganization(Organization updateOrganization, int ID) throws DAOException;
    void deactivateMultipleOrganizations(List<Integer> organizationIdentifiersList) throws DAOException;
    void activateOrganization(int organizationId) throws DAOException;
    Organization recoverOrganization(int organizationId) throws DAOException;
}
