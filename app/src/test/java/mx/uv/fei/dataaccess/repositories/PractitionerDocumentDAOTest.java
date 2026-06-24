package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.SQLException;
import java.util.List;

import mx.uv.fei.TestDatabaseSetup;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Profile;
import mx.uv.fei.config.annotation.test.StartEtiquetteTest;
import mx.uv.fei.dataaccess.exceptions.DAOException;
import mx.uv.fei.dataaccess.interfaces.IDatabaseConnection;
import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.enums.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PractitionerDocumentDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int PROFESSOR_ID = 68;
    private static final int ACCEPTED_DOCUMENT_ID = 1;
    private static final int SEEDED_INITIAL_COUNT = 4;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPractitionerDocumentDAO documentDAO;

    private PractitionerDocument finalDocument;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        finalDocument = new PractitionerDocument();
        finalDocument.setPractitionerId(PRACTITIONER_ID);
        finalDocument.setDocumentTypeCode(DocumentType.RELEASE_LETTER.getCode());
        finalDocument.setDocumentName("carta_liberacion.pdf");
        finalDocument.setStoredFileUrl("file:///SimuladorOneDrive_FEI/e5_carta_liberacion.pdf");
        finalDocument.setStatus(DocumentStatus.PENDING.getDatabaseValue());
    }

    @Test
    void insertDocument_NewTypeForPractitioner_ReturnsGeneratedId() throws DAOException {
        int generatedId = documentDAO.insertDocument(finalDocument);

        assertTrue(generatedId > 0);
    }

    @Test
    void insertDocument_DuplicateTypeForPractitioner_ThrowsDAOException() {
        PractitionerDocument duplicateInitial = new PractitionerDocument();
        duplicateInitial.setPractitionerId(PRACTITIONER_ID);
        duplicateInitial.setDocumentTypeCode(DocumentType.CURP.getCode());
        duplicateInitial.setDocumentName("curp_repetida.pdf");
        duplicateInitial.setStoredFileUrl("file:///SimuladorOneDrive_FEI/zz_curp.pdf");
        duplicateInitial.setStatus(DocumentStatus.PENDING.getDatabaseValue());

        assertThrows(DAOException.class, () -> documentDAO.insertDocument(duplicateInitial));
    }

    @Test
    void insertDocument_NonExistentPractitioner_ThrowsDAOException() {
        finalDocument.setPractitionerId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> documentDAO.insertDocument(finalDocument));
    }

    @Test
    void getDocumentsByPractitionerAndCategory_InitialDocuments_ReturnsExpectedCount() throws DAOException {
        List<PractitionerDocument> documents = documentDAO.getDocumentsByPractitionerAndCategory(
                PRACTITIONER_ID, DocumentCategory.INITIAL.getDatabaseValue());

        assertEquals(SEEDED_INITIAL_COUNT, documents.size());
    }

    @Test
    void getDocumentsByProfessor_WithSeededDocuments_ResolvesPractitionerName() throws DAOException {
        List<PractitionerDocument> documents = documentDAO.getDocumentsByProfessor(PROFESSOR_ID);

        assertNotNull(documents.get(0).getPractitionerName());
    }

    @Test
    void editDocument_AcceptedDocument_ResetsToPending() throws DAOException {
        documentDAO.editDocument(ACCEPTED_DOCUMENT_ID, "curp_corregida.pdf", "file:///SimuladorOneDrive_FEI/zz_curp_v2.pdf");

        PractitionerDocument editedDocument = findInitialDocumentById(ACCEPTED_DOCUMENT_ID);

        assertEquals(DocumentStatus.PENDING.getDatabaseValue(), editedDocument.getStatus());
    }

    @Test
    void rejectDocument_AcceptedDocument_SetsRejectedStatus() throws DAOException {
        documentDAO.rejectDocument(ACCEPTED_DOCUMENT_ID, "Documento ilegible");

        PractitionerDocument rejectedDocument = findInitialDocumentById(ACCEPTED_DOCUMENT_ID);

        assertEquals(DocumentStatus.REJECTED.getDatabaseValue(), rejectedDocument.getStatus());
    }

    @Test
    void acceptDocument_RejectedDocument_SetsAcceptedStatus() throws DAOException {
        documentDAO.rejectDocument(ACCEPTED_DOCUMENT_ID, "Documento ilegible");

        documentDAO.acceptDocument(ACCEPTED_DOCUMENT_ID);

        PractitionerDocument acceptedDocument = findInitialDocumentById(ACCEPTED_DOCUMENT_ID);
        assertEquals(DocumentStatus.ACCEPTED.getDatabaseValue(), acceptedDocument.getStatus());
    }

    @Test
    void acceptDocument_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> documentDAO.acceptDocument(NON_EXISTENT_ID));
    }

    @Test
    void documentExistsForType_UploadedType_ReturnsTrue() throws DAOException {
        boolean exists = documentDAO.documentExistsForType(PRACTITIONER_ID, DocumentType.CURP.getCode());

        assertTrue(exists);
    }

    @Test
    void documentExistsForType_NotUploadedType_ReturnsFalse() throws DAOException {
        boolean exists = documentDAO.documentExistsForType(PRACTITIONER_ID, DocumentType.RELEASE_LETTER.getCode());

        assertFalse(exists);
    }

    @Test
    void areAllDocumentsAccepted_AllInitialAccepted_ReturnsTrue() throws DAOException {
        boolean areAllAccepted = documentDAO.areAllDocumentsAccepted(
                PRACTITIONER_ID, DocumentCategory.INITIAL.getDatabaseValue());

        assertTrue(areAllAccepted);
    }

    @Test
    void areAllDocumentsAccepted_MissingFinalDocument_ReturnsFalse() throws DAOException {
        boolean areAllAccepted = documentDAO.areAllDocumentsAccepted(
                PRACTITIONER_ID, DocumentCategory.FINAL.getDatabaseValue());

        assertFalse(areAllAccepted);
    }

    private PractitionerDocument findInitialDocumentById(int documentId) throws DAOException {
        PractitionerDocument foundDocument = null;

        for (PractitionerDocument document : documentDAO.getDocumentsByPractitionerAndCategory(
                PRACTITIONER_ID, DocumentCategory.INITIAL.getDatabaseValue())) {
            if (document.getDocumentId() == documentId) {
                foundDocument = document;
            }
        }

        return foundDocument;
    }
}
