package mx.uv.fei.domain.dto;

import java.sql.Timestamp;
import java.util.Objects;

public class PractitionerDocument {

    private int documentId;
    private int practitionerId;
    private String documentName;
    private String documentTypeCode;
    private String documentTypeName;
    private String category;
    private String storedFileUrl;
    private String status;
    private String reviewComment;
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

    public String getDocumentTypeCode() { return documentTypeCode; }
    public void setDocumentTypeCode(String documentTypeCode) { this.documentTypeCode = documentTypeCode; }

    public String getDocumentTypeName() { return documentTypeName; }
    public void setDocumentTypeName(String documentTypeName) { this.documentTypeName = documentTypeName; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStoredFileUrl() { return storedFileUrl; }
    public void setStoredFileUrl(String storedFileUrl) { this.storedFileUrl = storedFileUrl; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getReviewComment() { return reviewComment; }
    public void setReviewComment(String reviewComment) { this.reviewComment = reviewComment; }

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
