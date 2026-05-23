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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class ProfessorManager {

    private static final Logger logger = LoggerFactory.getLogger(ProfessorManager.class);
    private final ProfessorDAO professorDAO;
    private final UserDAO userDAO;

    @Inject
    public ProfessorManager(UserDAO userDAO, ProfessorDAO professorDAO) {
        this.userDAO = userDAO;
        this.professorDAO = professorDAO;
    }

    public String registerNewProfessor(Professor professor) throws ManagerException {
        String tempPassword = PasswordManager.generatePassword();
        professor.setPassword(tempPassword);
        professor.setRole("Professor");
        professor.setStatus(UserStatus.PENDING);
        Validator.validateProfessorData(professor);

        try {
            int resultId = this.professorDAO.insertProfessor(professor);

            if (resultId <= 0) {
                throw new ManagerException("No se pudo completar el registro del profesor en el sistema.");
            }

            return tempPassword;

        } catch (DAOException e) {
            logger.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }

    public void inactivateMultipleProfessors(List<Integer> professorIdentifiersList) throws ManagerException {
        try {
            boolean isProcessSuccessful = userDAO.deactivateMultipleUsers(professorIdentifiersList);
            if (!isProcessSuccessful) {
                throw new ManagerException("No se pudieron inactivar los profesores seleccionados.");
            }
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error de base de datos al inactivar profesores.", dataAccessException);
        }
    }

    public List<Professor> getAllProfessors() throws ManagerException {
        try {
            return professorDAO.getAllProfessors();
        } catch (DAOException dataAccessException) {
            throw new ManagerException("Error al obtener la lista de profesores.", dataAccessException);
        }
    }
}