package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDAO;
import mx.uv.fei.domain.common.IFileBackup;
import mx.uv.fei.domain.common.IPractitionerParser;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;
import java.util.List;

@Component
public class PractitionerManager {

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
        } catch (DAOException e) {
            throw new ManagerException("Error de conexión con la base de datos.", e);
        }

        return temporaryPassword;
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
}