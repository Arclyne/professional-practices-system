package mx.uv.fei.dataaccess.interfaces;

import java.util.List;

import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.dto.Practitioner;

public interface IPractitionerDAO {

    int insertPractitioner(Practitioner practitioner) throws DAOException;

    Practitioner recoverPractitioner(int practitionerId) throws DAOException;

    List<Practitioner> getAllPractitioners() throws DAOException;

    void updatePractitioner(Practitioner practitionerToUpdate, int ID) throws DAOException;

    List<Practitioner> retrievePractitionersPendingAssignment() throws DAOException;

    List<Practitioner> retrieveAssignedPractitioners() throws DAOException;

    List<Practitioner> retrievePractitionersByProfessor(int professorId) throws DAOException;

    List<Practitioner> retrievePractitionersByProfessorAndPeriod(int professorId, int periodId) throws DAOException;

    List<Practitioner> retrievePractitionersByGroup(int groupId) throws DAOException;
}
