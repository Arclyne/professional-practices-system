package mx.uv.fei.domain.manager;

import java.util.UUID;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.ProfessorDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerExeption;

public class RegisterProfessorManager {

    private final IDatabaseConnection dbConnection;

    public RegisterProfessorManager(IDatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    public String registerNewProfessor(Professor professor) throws ManagerExeption {
        String tempPassword = "temp-" + UUID.randomUUID().toString().substring(0, 8);
        professor.setPassword(tempPassword);

        professor.setStatus("no activo");

        try {
            UserDAO userDAO = new UserDAO(dbConnection);
            ProfessorDAO professorDAO = new ProfessorDAO(dbConnection, userDAO);

            int resultId = professorDAO.insertProfessor(professor);

            if (resultId <= 0) {
                throw new ManagerExeption("No se pudo completar el registro del profesor en el sistema.");
            }

            return tempPassword;

        } catch (DAOException e) {
            e.printStackTrace();
            throw new ManagerExeption("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }
}