package mx.uv.fei.domain.manager;

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
                () -> documentManager.uploadDocument(INVALID_ID, anyFile));

        verifyNoInteractions(cloudStorageManager);
    }

    @Test
    void markDocumentAsReviewed_InvalidId_ThrowsBeforeQuerying() {
        assertThrows(ManagerException.class,
                () -> documentManager.markDocumentAsReviewed(INVALID_ID));

        verifyNoInteractions(documentDAO);
    }

    @Test
    void markDocumentAsReviewed_ValidId_DelegatesToDao() throws Exception {
        documentManager.markDocumentAsReviewed(VALID_DOCUMENT_ID);

        verify(documentDAO).markDocumentAsReviewed(VALID_DOCUMENT_ID);
    }

    @Test
    void getPractitionerDocuments_ValidId_ReturnsDaoResult() throws Exception {
        when(documentDAO.getDocumentsByPractitioner(VALID_PRACTITIONER_ID))
                .thenReturn(List.of(new PractitionerDocument()));

        List<PractitionerDocument> documents = documentManager.getPractitionerDocuments(VALID_PRACTITIONER_ID);

        assertEquals(1, documents.size());
    }
}
