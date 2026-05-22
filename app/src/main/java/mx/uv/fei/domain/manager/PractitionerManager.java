package mx.uv.fei.domain.manager;

import java.io.File;
import java.util.List;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Validator;
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

    private static final String REGISTER_ERROR_MESSAGE = "Fallo en el registro.";
    private static final String CONNECTION_ERROR_MESSAGE = "Error de conexion.";
    private static final String ROLE_PRACTITIONER = "Practitioner";
    private static final double DEFAULT_GRADE = 0.0;
    private static final int MINIMUM_VALID_ID = 1;

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
        String temporalPassword = PasswordManager.generateTemporaryPassword();

        practitioner.setPassword(temporalPassword);
        practitioner.setUserName(practitioner.getEnrollment());
        practitioner.setRole(ROLE_PRACTITIONER);
        practitioner.setStatus(UserStatus.PENDING);
        practitioner.setGrade(DEFAULT_GRADE);

        Validator.validatePractitioner(practitioner);

        try {
            int resultId = practitionerDAO.insertPractitioner(practitioner);

            if (resultId < MINIMUM_VALID_ID) {
                throw new ManagerException(REGISTER_ERROR_MESSAGE);
            }
        } catch (DAOException exception) {
            throw new ManagerException(CONNECTION_ERROR_MESSAGE, exception);
        }

        return temporalPassword;
    }

    public BatchRegistrationSummary registerPractitionerBatch(File batchFile, String coordinatorName) throws ManagerException {
        BatchRegistrationSummary registrationSummary = new BatchRegistrationSummary();

        fileBackup.backupFile(batchFile, coordinatorName);
        List<Practitioner> practitionersToRegister = practitionerParser.parsePractitioners(batchFile);

        for (Practitioner practitioner : practitionersToRegister) {
            try {
                registerNewPractitioner(practitioner);
                registrationSummary.incrementSuccess();
            } catch (ManagerException _) {
                registrationSummary.incrementFailure();
            }
        }

        return registrationSummary;
    }
}