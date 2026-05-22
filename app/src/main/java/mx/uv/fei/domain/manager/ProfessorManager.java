package mx.uv.fei.domain.manager;

import java.util.List;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.dataaccess.repositories.ProfessorDAO;
import mx.uv.fei.dataaccess.repositories.UserDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;


@Component
public class ProfessorManager {

    private static final String ROLE_PROFESSOR = "Professor";
    private static final String REGISTER_ERROR_MESSAGE = "No se pudo completar el registro del profesor en el sistema.";
    private static final String CONNECTION_ERROR_MESSAGE = "Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.";
    private static final String INACTIVATE_ERROR_MESSAGE = "No se pudieron inactivar los profesores seleccionados.";
    private static final String INACTIVATE_CONNECTION_ERROR_MESSAGE = "Error de base de datos al inactivar profesores.";
    private static final String GET_ALL_ERROR_MESSAGE = "Error al obtener la lista de profesores.";
    private static final int MINIMUM_VALID_ID = 1;

    private final ProfessorDAO professorDAO;
    private final UserDAO userDAO;

    @Inject
    public ProfessorManager(UserDAO userDAO, ProfessorDAO professorDAO) {
        this.userDAO = userDAO;
        this.professorDAO = professorDAO;
    }

    public String registerNewProfessor(Professor professor) throws ManagerException {
        String tempPassword = PasswordManager.generateTemporaryPassword();

        professor.setPassword(tempPassword);
        professor.setRole(ROLE_PROFESSOR);
        professor.setStatus(UserStatus.PENDING);

        Validator.validateProfessorData(professor);

        try {
            int resultId = professorDAO.insertProfessor(professor);

            if (resultId < MINIMUM_VALID_ID) {
                throw new ManagerException(REGISTER_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return tempPassword;
    }

    public void inactivateMultipleProfessors(List<Integer> professorIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = userDAO.deactivateMultipleUsers(professorIdentifiersList);

            if (!isProcessSuccessful) {
                throw new ManagerException(INACTIVATE_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(INACTIVATE_CONNECTION_ERROR_MESSAGE, exception);
        }
    }

    public List<Professor> getAllProfessors() throws ManagerException {
        List<Professor> professors;

        try {
            professors = professorDAO.getAllProfessors();
        } catch (DAOException exception) {
            throw new ManagerException(GET_ALL_ERROR_MESSAGE, exception);
        }

        return professors;
    }
}