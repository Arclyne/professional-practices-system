package mx.uv.fei.domain.manager;

import java.io.File;
import java.util.List;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.validators.UserValidator;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.domain.common.IFileBackup;
import mx.uv.fei.domain.common.IPractitionerParser;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class PractitionerManager {

    private static final String MSG_REGISTRATION_FAILED = "Registration failed in the database.";
    private static final String MSG_CONNECTION_ERROR = "Database connection error.";
    private static final String MSG_RETRIEVE_ERROR = "An error occurred while trying to retrieve the list of pending practitioners.";

    private static final String ROLE_PRACTITIONER = "Practitioner";

    private final IPractitionerDAO practitionerDAO;
    private final IFileBackup fileBackup;
    private final IPractitionerParser practitionerParser;

    @Inject
    public PractitionerManager(IPractitionerDAO practitionerDAO, IFileBackup fileBackup, IPractitionerParser practitionerParser) {
        this.practitionerDAO = practitionerDAO;
        this.fileBackup = fileBackup;
        this.practitionerParser = practitionerParser;
    }

    public String registerNewPractitioner(Practitioner practitioner) throws ManagerException {
        String temporalPassword = PasswordManager.generatePassword();

        practitioner.setPassword(temporalPassword);
        practitioner.setUserName(practitioner.getEnrollment());
        practitioner.setRole(ROLE_PRACTITIONER);
        practitioner.setStatus(UserStatus.PENDING);
        practitioner.setGrade(0.0);

        UserValidator.validatePractitioner(practitioner);

        try {
            int resultId = practitionerDAO.insertPractitioner(practitioner);

            if (resultId <= 0) {
                throw new ManagerException(MSG_REGISTRATION_FAILED);
            }

        } catch (DAOException exception) {
            throw new ManagerException(MSG_CONNECTION_ERROR, exception);
        }

        return temporalPassword;
    }

    public BatchRegistrationSummary registerPractitionerBatch(File batchFile, String coordinatorName) throws ManagerException {
        fileBackup.backupFile(batchFile, coordinatorName);

        List<Practitioner> practitionersToRegister = practitionerParser.parsePractitioners(batchFile);
        BatchRegistrationSummary registrationSummary = new BatchRegistrationSummary();

        for (Practitioner practitioner : practitionersToRegister) {
            try {
                registerNewPractitioner(practitioner);
                registrationSummary.incrementSuccess();
            } catch (ManagerException exception) {
                registrationSummary.incrementFailure();
            }
        }

        return registrationSummary;
    }

    public List<Practitioner> retrievePractitionersPendingAssignment() throws ManagerException {
        List<Practitioner> retrievedPendingList;

        try {
            retrievedPendingList = practitionerDAO.retrievePractitionersPendingAssignment();
        } catch (DAOException exception) {
            throw new ManagerException(MSG_RETRIEVE_ERROR, exception);
        }

        return retrievedPendingList;
    }

    public List<Practitioner> retrieveAssignedPractitioners() throws ManagerException {
        List<Practitioner> retrievedAssignedList;

        try {
            retrievedAssignedList = practitionerDAO.retrieveAssignedPractitioners();
        } catch (DAOException exception) {
            throw new ManagerException("Ocurrió un error al intentar recuperar la lista de practicantes asignados.", exception);
        }

        return retrievedAssignedList;
    }

}