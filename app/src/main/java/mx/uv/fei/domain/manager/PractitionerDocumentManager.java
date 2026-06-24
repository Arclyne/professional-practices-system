package mx.uv.fei.domain.manager;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.common.validators.BaseValidator;
import mx.uv.fei.domain.common.validators.FieldLengthLimits;
import mx.uv.fei.domain.dto.PractitionerDocument;
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

    @Inject
    public PractitionerDocumentManager(IPractitionerDocumentDAO documentDAO, CloudStorageManager cloudStorageManager) {
        this.documentDAO = documentDAO;
        this.cloudStorageManager = cloudStorageManager;
    }

    public void uploadDocument(int practitionerId, File file, DocumentType documentType) throws ManagerException {
        BaseValidator.validateId(practitionerId, "El practicante indicado no es válido.");

        String storedFileUrl = cloudStorageManager.uploadEvidenceFile(file);
        String documentName = file.getName();
        BaseValidator.validateMaxLength(documentName, FieldLengthLimits.DOCUMENT_NAME_MAX,
                "El nombre del documento no puede exceder " + FieldLengthLimits.DOCUMENT_NAME_MAX + " caracteres.");

        PractitionerDocument document = buildPendingDocument(practitionerId, documentName, storedFileUrl, documentType);

        try {
            int generatedId = documentDAO.insertDocument(document);
            if (generatedId <= 0) {
                throw new ManagerException("No se pudo registrar el documento subido.");
            }
        } catch (DAOException e) {
            log.error("No se pudo guardar el documento del practicante {}.", practitionerId, e);
            throw new ManagerException("Ocurrió un error al guardar el documento.", e);
        }
    }

    public List<PractitionerDocument> getPractitionerDocuments(int practitionerId, DocumentType documentType) throws ManagerException {
        try {
            return documentDAO.getDocumentsByPractitionerAndType(practitionerId, documentType.getDatabaseValue());
        } catch (DAOException e) {
            log.error("Error al cargar los documentos del practicante {}.", practitionerId, e);
            throw new ManagerException("Ocurrió un error al cargar los documentos.", e);
        }
    }

    public List<PractitionerDocument> getDocumentsByProfessor(int professorId) throws ManagerException {
        try {
            return documentDAO.getDocumentsByProfessor(professorId);
        } catch (DAOException e) {
            log.error("Error al cargar los documentos de los practicantes del profesor {}.", professorId, e);
            throw new ManagerException("Ocurrió un error al cargar los documentos de los practicantes.", e);
        }
    }

    public void acceptDocument(int documentId) throws ManagerException {
        BaseValidator.validateId(documentId, "El documento indicado no es válido.");
        try {
            documentDAO.acceptDocument(documentId);
        } catch (DAOException e) {
            log.error("Error al aceptar el documento {}.", documentId, e);
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
            log.error("Error al rechazar el documento {}.", documentId, e);
            throw new ManagerException("No se pudo rechazar el documento.", e);
        }
    }

    public boolean areAllInitialDocumentsAccepted(int practitionerId) throws ManagerException {
        return areAllDocumentsAccepted(practitionerId, DocumentType.INITIAL);
    }

    public boolean areAllFinalDocumentsAccepted(int practitionerId) throws ManagerException {
        return areAllDocumentsAccepted(practitionerId, DocumentType.FINAL);
    }

    private boolean areAllDocumentsAccepted(int practitionerId, DocumentType documentType) throws ManagerException {
        try {
            return documentDAO.areAllDocumentsAccepted(practitionerId, documentType.getDatabaseValue());
        } catch (DAOException e) {
            log.error("Error al verificar los documentos aceptados del practicante {}.", practitionerId, e);
            throw new ManagerException("Ocurrió un error al verificar los documentos del practicante.", e);
        }
    }

    private PractitionerDocument buildPendingDocument(int practitionerId, String documentName, String storedFileUrl,
                                                      DocumentType documentType) {
        PractitionerDocument document = new PractitionerDocument();
        document.setPractitionerId(practitionerId);
        document.setDocumentName(documentName);
        document.setDocumentType(documentType.getDatabaseValue());
        document.setStoredFileUrl(storedFileUrl);
        document.setStatus(DocumentStatus.PENDING.getDatabaseValue());
        return document;
    }
}
