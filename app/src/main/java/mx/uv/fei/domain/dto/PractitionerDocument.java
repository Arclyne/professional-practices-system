package mx.uv.fei.domain.dto;

import java.sql.Timestamp;
import java.util.Objects;

/**
 * Representa un documento general subido por un practicante a su expediente de prácticas.
 *
 * El nombre original del archivo se conserva para mostrarlo al usuario, mientras que la
 * URL almacenada es única por subida, lo que permite distinguir archivos con el mismo nombre.
 *
 * @author Angel Gabriel Aguilar Hernandez
 * @version 1.0
 */
public class PractitionerDocument {

    private int documentId;
    private int practitionerId;
    private String documentName;
    private String storedFileUrl;
    private String status;
    private Timestamp uploadDate;
    private Timestamp reviewDate;
    private String practitionerName;
    private String practitionerEnrollment;

    public PractitionerDocument() {}

    public int getDocumentId() { return documentId; }
    public void setDocumentId(int documentId) { this.documentId = documentId; }

    public int getPractitionerId() { return practitionerId; }
    public void setPractitionerId(int practitionerId) { this.practitionerId = practitionerId; }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public String getStoredFileUrl() { return storedFileUrl; }
    public void setStoredFileUrl(String storedFileUrl) { this.storedFileUrl = storedFileUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getUploadDate() { return uploadDate; }
    public void setUploadDate(Timestamp uploadDate) { this.uploadDate = uploadDate; }

    public Timestamp getReviewDate() { return reviewDate; }
    public void setReviewDate(Timestamp reviewDate) { this.reviewDate = reviewDate; }

    public String getPractitionerName() { return practitionerName; }
    public void setPractitionerName(String practitionerName) { this.practitionerName = practitionerName; }

    public String getPractitionerEnrollment() { return practitionerEnrollment; }
    public void setPractitionerEnrollment(String practitionerEnrollment) { this.practitionerEnrollment = practitionerEnrollment; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            PractitionerDocument other = (PractitionerDocument) obj;
            isEqual = Objects.equals(this.storedFileUrl, other.storedFileUrl);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storedFileUrl);
    }
}
