package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.domain.dto.Organization;

import java.util.List;

import mx.uv.fei.dataacces.exceptions.DAOException;

public interface IOrganizationDAO {
    boolean insertOrganization(Organization organizacion) throws DAOException;

    Organization recoverOrganization(String organizationName) throws DAOException;

    List<Organization> getAllOrganization() throws DAOException;

    boolean updateOrganization(Organization upDateOrganization, int ID) throws DAOException;

    boolean deactivateMultipleOrganizations(List<Integer> organizationIdentifiersList) throws DAOException;
}
