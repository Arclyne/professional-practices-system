package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.interfaces.IUserDAO;
import mx.uv.fei.domain.common.IFileBackup;
import mx.uv.fei.domain.common.IPractitionerParser;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;

@Component
public class PractitionerManager {

    private static final Logger log = LoggerFactory.getLogger(PractitionerManager.class);
    private static final String ROLE_PRACTITIONER = "Practitioner";

    private final IPractitionerDAO practitionerDAO;
    private final IUserDAO userDAO;
    private final IFileBackup fileBackup;
    private final IPractitionerParser practitionerParser;
    private final GroupEnrollmentManager groupEnrollmentManager;

    @Inject
    public PractitionerManager(IPractitionerDAO practitionerDAO, IUserDAO userDAO, IFileBackup fileBackup,
                               IPractitionerParser practitionerParser, GroupEnrollmentManager groupEnrollmentManager) {
        this.practitionerDAO = practitionerDAO;
        this.userDAO = userDAO;
        this.fileBackup = fileBackup;
        this.practitionerParser = practitionerParser;
        this.groupEnrollmentManager = groupEnrollmentManager;
    }

    public void inactivatePractitioner(int practitionerId) throws ManagerException {
        try {
            userDAO.deactivateUser(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al inactivar el practicante.", e);
        }
    }

    public void activatePractitioner(int practitionerId) throws ManagerException {
        try {
            userDAO.activateUser(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error de base de datos al activar el practicante.", e);
        }
    }

    public String registerNewPractitioner(Practitioner practitioner) throws ManagerException {
        UserValidator.validatePractitionerText(practitioner);
        practitioner.setEnrollment(practitioner.getEnrollment().toUpperCase());

        verifyEnrollmentAvailability(practitioner.getEnrollment());

        String temporaryPassword = PasswordManager.generatePassword();
        practitioner.setPassword(temporaryPassword);
        practitioner.setUserName(practitioner.getEnrollment());
        practitioner.setRole(ROLE_PRACTITIONER);
        practitioner.setStatus(UserStatus.PENDING);
        practitioner.setGrade(0.0);

        try {
            int generatedId = practitionerDAO.insertPractitioner(practitioner);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo completar el registro del practicante.");
            }
            enrollPractitionerIfGroupSelected(generatedId, practitioner.getGroupId());
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Error de conexión con la base de datos.", e);
        }

        return temporaryPassword;
    }

    private void enrollPractitionerIfGroupSelected(int practitionerId, Integer groupId) throws ManagerException {
        if (groupId != null && groupId > 0) {
            groupEnrollmentManager.enrollPractitionerInGroup(practitionerId, groupId);
        }
    }

    public BatchRegistrationSummary registerPractitionerBatch(File batchFile, String coordinatorName) throws ManagerException {
        fileBackup.backupFile(batchFile, coordinatorName);
        List<Practitioner> practitioners = practitionerParser.parsePractitioners(batchFile);
        BatchRegistrationSummary registrationSummary = new BatchRegistrationSummary();

        for (Practitioner practitioner : practitioners) {
            try {
                registerNewPractitioner(practitioner);
                registrationSummary.incrementSuccess();
            } catch (ManagerException e) {
                registrationSummary.incrementFailure();
            }
        }

        return registrationSummary;
    }

    public List<Practitioner> retrievePractitionersPendingAssignment() throws ManagerException {
        try {
            return practitionerDAO.retrievePractitionersPendingAssignment();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los practicantes pendientes de asignación.", e);
        }
    }

    public List<Practitioner> retrieveAssignedPractitioners() throws ManagerException {
        try {
            return practitionerDAO.retrieveAssignedPractitioners();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los practicantes asignados.", e);
        }
    }

    public List<Practitioner> retrievePractitionersByProfessor(int professorId) throws ManagerException {
        try {
            return practitionerDAO.retrievePractitionersByProfessor(professorId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los practicantes del profesor.", e);
        }
    }

    public List<Practitioner> retrievePractitionersByProfessorAndPeriod(int professorId, int periodId)
            throws ManagerException {
        try {
            return practitionerDAO.retrievePractitionersByProfessorAndPeriod(professorId, periodId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los practicantes del profesor.", e);
        }
    }

    public List<Practitioner> retrievePractitionersByGroup(int groupId) throws ManagerException {
        try {
            return practitionerDAO.retrievePractitionersByGroup(groupId);
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar los practicantes del grupo.", e);
        }
    }

    public List<Practitioner> getAllPractitioners() throws ManagerException {
        try {
            return practitionerDAO.getAllPractitioners();
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al recuperar la lista de practicantes.", e);
        }
    }

    public Practitioner getPractitionerById(int practitionerId) throws ManagerException {
        try {
            return practitionerDAO.recoverPractitioner(practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("No se pudo recuperar la información del practicante.", e);
        }
    }

    public void updatePractitioner(Practitioner practitioner, int practitionerId) throws ManagerException {
        UserValidator.validatePractitionerForUpdate(practitioner);

        try {
            practitionerDAO.updatePractitioner(practitioner, practitionerId);
        } catch (DAOException e) {
            throw new ManagerException("Error de conexión con la base de datos.", e);
        }
    }

    private void verifyEnrollmentAvailability(String enrollment) throws ManagerException {
        try {
            User existingUser = userDAO.getUserByUserName(enrollment);
            if (existingUser != null && existingUser.getUserName() != null) {
                throw new ManagerException("La matrícula " + enrollment + " ya se encuentra registrada en el sistema.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Error al verificar la disponibilidad de la matrícula.", e);
        }
    }
}