package mx.uv.fei.domain.manager;

import java.io.File;
import java.util.List;
import java.util.UUID;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Validator;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.BatchRegistrationSummary;
import mx.uv.fei.domain.common.IFileBackup;
import mx.uv.fei.domain.common.IPractitionerParser;
import mx.uv.fei.dataacces.interfaces.IPractitionerDAO;
import mx.uv.fei.dataacces.exceptions.DAOException;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class PractitionerManager {

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
        String temporalPassword = generateTemporalPassword();

        practitioner.setPassword(temporalPassword);
        practitioner.setUserName(practitioner.getEnrollment());
        practitioner.setRole("Practitioner");
        practitioner.setStatus(UserStatus.PENDING);
        practitioner.setGrade(0.0);

        Validator.validatePractitioner(practitioner);

        try {
            int resultId = practitionerDAO.insertPractitioner(practitioner);

            if (resultId <= 0) {
                throw new ManagerException("Fallo en el registro.");
            }

            return temporalPassword;

        } catch (DAOException exception) {
            throw new ManagerException("Error de conexion.", exception);
        }
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

    private String generateTemporalPassword() {
        return "temp-" + UUID.randomUUID().toString().substring(0, 8);
    }
}