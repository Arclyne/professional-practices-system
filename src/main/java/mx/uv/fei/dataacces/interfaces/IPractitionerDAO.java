package mx.uv.fei.dataacces.interfaces;

import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.dto.Practitioner;

public interface IPractitionerDAO {
    int insertPractitioner(Practitioner practitioner) throws DAOException;
    Practitioner recoverPractitioner(int practitionerId) throws DAOException;
}