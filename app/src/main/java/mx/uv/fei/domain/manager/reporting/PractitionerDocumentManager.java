package mx.uv.fei.domain.manager.reporting;
import mx.uv.fei.domain.manager.academic.PracticeAccessManager;
import mx.uv.fei.domain.manager.infrastructure.CloudStorageManager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.FieldLengthLimits;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.enums.DocumentType;
import mx.uv.fei.domain.exceptions.ManagerException;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class PractitionerDocumentManager {

    private static final Logger log = LoggerFactory.getLogger(PractitionerDocumentManager.class);
    private static final int REVIEW_COMMENT_MAX = 500;

    private final IPractitionerDocumentDAO documentDAO;
    private final CloudStorageManager cloudStorageManager;
    private final PracticeAccessManager practiceAccessManager;

    @Inject
    public PractitionerDocumentManager(IPractitionerDocumentDAO documentDAO, CloudStorageManager cloudStorageManager,
                                       PracticeAccessManager practiceAccessManager) {
        this.documentDAO = documentDAO;
        this.cloudStorageManager = cloudStorageManager;
        this.practiceAccessManager = practiceAccessManager;
    }

    public void uploadDocument(int practitionerId, DocumentType documentType, File file) throws ManagerException {
        BaseValidator.validateId(practitionerId, "El practicante indicado no es válido.");
        practiceAccessManager.ensureSubmissionsAllowed(practitionerId);
        validateDocumentNotUploaded(practitionerId, documentType);

        String storedFileUrl = cloudStorageManager.uploadEvidenceFile(file);
        String documentName = validatedDocumentName(file);
        PractitionerDocument document = buildPendingDocument(practitionerId, documentType, documentName, storedFileUrl);

        try {
            int generatedId = documentDAO.insertDocument(document);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar el documento subido.");
            }
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al guardar el documento.", e);
        }
    }

    public void editDocument(int documentId, File file) throws ManagerException {
        BaseValidator.validateId(documentId, "El documento indicado no es válido.");

        String storedFileUrl = cloudStorageManager.uploadEvidenceFile(file);
        String documentName = validatedDocumentName(file);

        try {
            documentDAO.editDocument(documentId, documentName, storedFileUrl);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al actualizar el documento.", e);
        }
    }

    public List<PractitionerDocument> getPractitionerDocuments(int practitionerId, DocumentCategory category) throws ManagerException {
        try {
            return documentDAO.getDocumentsByPractitionerAndCategory(practitionerId, category.getDatabaseValue());
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al cargar los documentos.", e);
        }
    }

    public List<PractitionerDocument> getDocumentsByProfessor(int professorId) throws ManagerException {
        try {
            return documentDAO.getDocumentsByProfessor(professorId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("Ocurrió un error al cargar los documentos de los practicantes.", e);
        }
    }

    public void acceptDocument(int documentId) throws ManagerException {
        BaseValidator.validateId(documentId, "El documento indicado no es válido.");
        try {
            documentDAO.acceptDocument(documentId);
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo aceptar el documento.", e);
        }
    }

    public void rejectDocument(int documentId, String reviewComment) throws ManagerException {
        BaseValidator.validateId(documentId, "El documento indicado no es válido.");
        BaseValidator.validateString(reviewComment, "Debes indicar el motivo del rechazo del documento.");
        BaseValidator.validateMaxLength(reviewComment, REVIEW_COMMENT_MAX,
                "El motivo del rechazo no puede exceder " + REVIEW_COMMENT_MAX + " caracteres.");
        try {
            documentDAO.rejectDocument(documentId, reviewComment.trim());
        } catch (DAOException e) {
            log.error(e.getMessage(), e);
            throw new ManagerException("No se pudo rechazar el documento.", e);
        }
    }

    public boolean areAllInitialDocumentsAccepted(int practitionerId) throws ManagerException {
        return areAllDocumentsAccepted(practitionerId, DocumentCategory.INITIAL);
    }

    private boolean areAllDocumentsAccepted(int practitionerId, DocumentCategory category) throws ManagerException {
        try {
            return documentDAO.areAllDocumentsAccepted(practitionerId, category.getDatabaseValue());
        } catch (DAOException e) {
            log.error("Error al verificar los documentos aceptados del practicante {}.", practitionerId, e);
            throw new ManagerException("Ocurrió un error al verificar los documentos del practicante.", e);
        }
    }

    private void validateDocumentNotUploaded(int practitionerId, DocumentType documentType) throws ManagerException {
        try {
            if (documentDAO.documentExistsForType(practitionerId, documentType.getCode())) {
                throw new ManagerException("Ya subiste este documento. Usa la opción Editar para reemplazarlo.");
            }
        } catch (DAOException e) {
            throw new ManagerException("Ocurrió un error al verificar el documento.", e);
        }
    }

    private String validatedDocumentName(File file) throws ManagerException {
        String documentName = file.getName();
        BaseValidator.validateMaxLength(documentName, FieldLengthLimits.DOCUMENT_NAME_MAX,
                "El nombre del documento no puede exceder " + FieldLengthLimits.DOCUMENT_NAME_MAX + " caracteres.");
        return documentName;
    }

    private PractitionerDocument buildPendingDocument(int practitionerId, DocumentType documentType,
                                                      String documentName, String storedFileUrl) {
        PractitionerDocument document = new PractitionerDocument();
        document.setPractitionerId(practitionerId);
        document.setDocumentTypeCode(documentType.getCode());
        document.setDocumentName(documentName);
        document.setStoredFileUrl(storedFileUrl);
        document.setStatus(DocumentStatus.PENDING.getDatabaseValue());
        return document;
    }
}
