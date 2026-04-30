package mx.uv.fei.domain.manager;

import java.util.UUID;

import mx.uv.fei.domain.common.CommonValidator;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.dataacces.interfaces.IDatabaseConnection;
import mx.uv.fei.dataacces.repositories.ProfessorDAO;
import mx.uv.fei.dataacces.repositories.UserDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.exceptions.ManagerException;

public class RegisterProfessorManager {
    private UserDAO userDAO;
    private ProfessorDAO professorDAO;

    public RegisterProfessorManager(IDatabaseConnection dbConnection) {
        this.userDAO = new UserDAO(dbConnection);
        this.professorDAO = new ProfessorDAO(dbConnection, userDAO);
    }

    public String registerNewProfessor(Professor professor) throws ManagerException {
        String tempPassword = this.generatePassword();
        professor.setPassword(tempPassword);
        professor.setStatus("no activo");
        CommonValidator.validateProfessorData(professor);

        try {
            int resultId = this.professorDAO.insertProfessor(professor);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del profesor en el sistema.");
            }

            return tempPassword;

        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }

    private String generatePassword() {
        return "temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}