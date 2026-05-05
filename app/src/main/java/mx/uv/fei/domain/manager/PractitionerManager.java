package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.PractitionerDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

public class PractitionerManager {

    private PractitionerDAO practitionerDAO;

    public PractitionerManager(IDatabaseConnection dbConnection) {
        UserDAO userDAO = new UserDAO(dbConnection);
        practitionerDAO = new PractitionerDAO(dbConnection, userDAO);
    }

    public String registerNewPractitioner(Practitioner practitioner) throws ManagerException {
        String temporalPassword = "temp-" + UUID.randomUUID().toString().substring(0, 8);

        practitioner.setPassword(temporalPassword);
        practitioner.setUserName(practitioner.getEnrollment());
        practitioner.setRole("Practitioner");
        practitioner.setStatus("Pendiente");
        practitioner.setGrade(0.0);

        Validator.validatePractitioner(practitioner);

        try {
            int resultId = this.practitionerDAO.insertPractitioner(practitioner);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del practicante en el sistema.");
            }

            return temporalPassword;

        } catch (DAOException e) {
            throw new ManagerException(
                    "Ocurrió un problema al guardar en la base de datos. Por favor, intente más tarde.", e);
        }
    }
}