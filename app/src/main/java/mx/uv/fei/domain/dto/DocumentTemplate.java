package mx.uv.fei.domain.dto;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class DocumentTemplate {

    private String templateId;
    private String templateName;
    private String documentType;
    private String bodyContent;
    private String createdByUserName;
    private LocalDate createdAt;

    public DocumentTemplate() {
        this.templateId = UUID.randomUUID().toString();
        this.createdAt = LocalDate.now();
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getBodyContent() {
        return bodyContent;
    }

    public void setBodyContent(String bodyContent) {
        this.bodyContent = bodyContent;
    }

    public String getCreatedByUserName() {
        return createdByUserName;
    }

    public void setCreatedByUserName(String createdByUserName) {
        this.createdByUserName = createdByUserName;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            DocumentTemplate other = (DocumentTemplate) obj;
            isEqual = Objects.equals(this.templateName, other.templateName) &&
                    Objects.equals(this.documentType, other.documentType);
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(templateName, documentType);
    }

    @Override
    public String toString() {
        return templateName + " (" + documentType + ")";
    }
}