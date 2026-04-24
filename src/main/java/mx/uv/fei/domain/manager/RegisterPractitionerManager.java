package mx.uv.fei.domain.manager;


import java.util.UUID;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.PractitionerDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;


import mx.uv.fei.domain.exceptions.ManagerExeption;

public class RegisterPractitionerManager {

    private final IDatabaseConnection dbConnection;

    public RegisterPractitionerManager(IDatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public String registerNewPractitioner(Practitioner practitioner) throws ManagerExeption {
        String tempPassword = "temp-" + UUID.randomUUID().toString().substring(0, 8);
        practitioner.setPassword(tempPassword);
        practitioner.setStatus("no activo");
        practitioner.setGrade(0.0);

        try {
            UserDAO userDAO = new UserDAO(dbConnection);
            PractitionerDAO practitionerDAO = new PractitionerDAO(dbConnection, userDAO);

            int resultId = practitionerDAO.insertPractitioner(practitioner);

            if (resultId <= 0) {
                throw new ManagerExeption("No se pudo completar el registro del practicante en el sistema.");
            }

            return tempPassword;

        } catch (DAOException e) {
            e.printStackTrace();
            throw new ManagerExeption("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }
}