package mx.uv.fei.domain.manager.reporting;
import mx.uv.fei.domain.manager.infrastructure.CloudStorageManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;

import mx.uv.fei.dataaccess.interfaces.IPractitionerDocumentDAO;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.enums.DocumentType;
import mx.uv.fei.domain.exceptions.ManagerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PractitionerDocumentManagerTest {

    private static final int VALID_PRACTITIONER_ID = 123;
    private static final int INVALID_ID = 0;
    private static final int VALID_DOCUMENT_ID = 1;

    private IPractitionerDocumentDAO documentDAO;
    private CloudStorageManager cloudStorageManager;
    private PractitionerDocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentDAO = mock(IPractitionerDocumentDAO.class);
        cloudStorageManager = mock(CloudStorageManager.class);
        documentManager = new PractitionerDocumentManager(documentDAO, cloudStorageManager);
    }

    @Test
    void uploadDocument_InvalidPractitionerId_ThrowsBeforeUploading() {
        File anyFile = mock(File.class);

        assertThrows(ManagerException.class,
                () -> documentManager.uploadDocument(INVALID_ID, DocumentType.CURP, anyFile));

        verifyNoInteractions(cloudStorageManager);
    }

    @Test
    void uploadDocument_TypeAlreadyUploaded_ThrowsBeforeUploading() throws Exception {
        File anyFile = mock(File.class);
        when(documentDAO.documentExistsForType(VALID_PRACTITIONER_ID, DocumentType.CURP.getCode())).thenReturn(true);

        assertThrows(ManagerException.class,
                () -> documentManager.uploadDocument(VALID_PRACTITIONER_ID, DocumentType.CURP, anyFile));

        verifyNoInteractions(cloudStorageManager);
    }

    @Test
    void acceptDocument_InvalidId_ThrowsBeforeQuerying() {
        assertThrows(ManagerException.class,
                () -> documentManager.acceptDocument(INVALID_ID));

        verifyNoInteractions(documentDAO);
    }

    @Test
    void acceptDocument_ValidId_DelegatesToDao() throws Exception {
        documentManager.acceptDocument(VALID_DOCUMENT_ID);

        verify(documentDAO).acceptDocument(VALID_DOCUMENT_ID);
    }

    @Test
    void rejectDocument_BlankReason_ThrowsBeforeQuerying() {
        assertThrows(ManagerException.class,
                () -> documentManager.rejectDocument(VALID_DOCUMENT_ID, "   "));

        verifyNoInteractions(documentDAO);
    }

    @Test
    void getPractitionerDocuments_ValidId_ReturnsDaoResult() throws Exception {
        when(documentDAO.getDocumentsByPractitionerAndCategory(VALID_PRACTITIONER_ID, DocumentCategory.INITIAL.getDatabaseValue()))
                .thenReturn(List.of(new PractitionerDocument()));

        List<PractitionerDocument> documents = documentManager.getPractitionerDocuments(
                VALID_PRACTITIONER_ID, DocumentCategory.INITIAL);

        assertEquals(1, documents.size());
    }
}
