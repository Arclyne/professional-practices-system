package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IProfessorDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

@Component
public class ProfessorManager {

    private static final Logger log = LoggerFactory.getLogger(ProfessorManager.class);

    private final IProfessorDAO professorDAO;
    private final IUserDAO userDAO;

    @Inject
    public ProfessorManager(IUserDAO userDAO, IProfessorDAO professorDAO) {
        this.userDAO = userDAO;
        this.professorDAO = professorDAO;
    }

    public String registerNewProfessor(Professor professor) throws ManagerException {
        String temporaryPassword = PasswordManager.generatePassword();
        professor.setPassword(temporaryPassword);
        professor.setRole("Professor");
        professor.setStatus(UserStatus.PENDING);
        UserValidator.validateProfessorData(professor);

        try {
            int generatedId = professorDAO.insertProfessor(professor);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del profesor en el sistema.");
            }
            return temporaryPassword;
        } catch (DAOException e) {
            log.error("Error al insertar el profesor.", e);
            String rootError= Controller.getRootError(e);
            if (rootError.contains(professor.getEmail())){
                throw new ManagerException("El correo ya está registrado.", e);
            }else if(rootError.contains(String.valueOf(professor.getId()))) {
                throw new ManagerException("Ya existe un registro con ese numero de personal", e);
            }else{
                throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
            }
        }
    }

    public void inactivateMultipleProfessors(List<Integer> professorIds) throws ManagerException {
        try {
            userDAO.deactivateMultipleUsers(professorIds);
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al inactivar profesores.", e);
        }
    }

    public void inactivateProfessor(int professorId) throws ManagerException {
        try {
            userDAO.deactivateUser(professorId);
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al inactivar el profesor.", e);
        }
    }

    public void activateProfessor(int professorId) throws ManagerException {
        try {
            userDAO.activateUser(professorId);
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al activar el profesor.", e);
        }
    }

    public List<Professor> getAllProfessors() throws ManagerException {
        try {
            return professorDAO.getAllProfessors();
        } catch (DAOException e) {
            throw new ManagerException("Error al obtener la lista de profesores.", e);
        }
    }

    public Professor getProfessorById(int professorId) throws ManagerException {
        try {
            return professorDAO.recoverProfessor(professorId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar la información del profesor.", e);
        }
    }

    public void updateProfessor(Professor professor, int professorId) throws ManagerException {
        UserValidator.validateProfessorForUpdate(professor);

        try {
            professorDAO.updateProfessor(professor, professorId);
        } catch (DAOException e) {
            log.error("Error al actualizar el profesor.", e);
            throw new ManagerException("Ocurrió un problema de conexión con el servidor. Por favor, intente más tarde.", e);
        }
    }
}