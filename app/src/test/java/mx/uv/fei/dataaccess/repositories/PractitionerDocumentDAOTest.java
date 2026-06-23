package mx.uv.fei.dataaccess.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
import mx.uv.fei.domain.enums.DocumentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@StartEtiquetteTest
@Profile("test")
public class PractitionerDocumentDAOTest {

    private static final int PRACTITIONER_ID = 123;
    private static final int PENDING_DOCUMENT_ID = 1;
    private static final int SEEDED_DOCUMENTS_COUNT = 2;
    private static final int NON_EXISTENT_ID = 9999;

    @Inject
    private IDatabaseConnection dbConnection;

    @Inject
    private IPractitionerDocumentDAO documentDAO;

    private PractitionerDocument newDocument;

    @BeforeEach
    void setUp() throws SQLException {
        TestDatabaseSetup.initialize(dbConnection);

        newDocument = new PractitionerDocument();
        newDocument.setPractitionerId(PRACTITIONER_ID);
        newDocument.setDocumentName("carta_aceptacion.pdf");
        newDocument.setStoredFileUrl("file:///SimuladorOneDrive_FEI/c9d0e1f2_carta_aceptacion.pdf");
        newDocument.setStatus(DocumentStatus.PENDING.getDatabaseValue());
    }

    @Test
    void insertDocument_ValidDocument_ReturnsGeneratedId() throws DAOException {
        int generatedId = documentDAO.insertDocument(newDocument);

        assertTrue(generatedId > 0);
    }

    @Test
    void insertDocument_SameNameDifferentUrl_KeepsBothDocuments() throws DAOException {
        documentDAO.insertDocument(newDocument);

        List<PractitionerDocument> documents = documentDAO.getDocumentsByPractitioner(PRACTITIONER_ID);

        assertEquals(SEEDED_DOCUMENTS_COUNT + 1, documents.size());
    }

    @Test
    void getDocumentsByPractitioner_WithSeededDocuments_ReturnsExpectedCount() throws DAOException {
        List<PractitionerDocument> documents = documentDAO.getDocumentsByPractitioner(PRACTITIONER_ID);

        assertEquals(SEEDED_DOCUMENTS_COUNT, documents.size());
    }

    @Test
    void getAllDocuments_WithSeededDocuments_ResolvesPractitionerName() throws DAOException {
        List<PractitionerDocument> documents = documentDAO.getAllDocuments();

        assertNotNull(documents.get(0).getPractitionerName());
    }

    @Test
    void markDocumentAsReviewed_PendingDocument_SetsReviewedStatus() throws DAOException {
        documentDAO.markDocumentAsReviewed(PENDING_DOCUMENT_ID);

        PractitionerDocument reviewedDocument = findDocumentById(PENDING_DOCUMENT_ID);

        assertEquals(DocumentStatus.REVIEWED.getDatabaseValue(), reviewedDocument.getStatus());
    }

    @Test
    void insertDocument_NonExistentPractitioner_ThrowsDAOException() {
        newDocument.setPractitionerId(NON_EXISTENT_ID);

        assertThrows(DAOException.class, () -> documentDAO.insertDocument(newDocument));
    }

    @Test
    void markDocumentAsReviewed_NonExistentId_ThrowsDAOException() {
        assertThrows(DAOException.class, () -> documentDAO.markDocumentAsReviewed(NON_EXISTENT_ID));
    }

    private PractitionerDocument findDocumentById(int documentId) throws DAOException {
        PractitionerDocument foundDocument = null;

        for (PractitionerDocument document : documentDAO.getDocumentsByPractitioner(PRACTITIONER_ID)) {
            if (document.getDocumentId() == documentId) {
                foundDocument = document;
            }
        }

        return foundDocument;
    }
}
