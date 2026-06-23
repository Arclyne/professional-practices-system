package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerDocumentManager;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.awt.Desktop;
import java.net.URI;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Component
public class CoordinatorDocumentsController {

    private static final String NO_DATE_LABEL = "—";
    private static final String FILE_OPEN_ERROR = "No se pudo abrir el documento.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TextField searchTextField;
    @FXML private TableView<PractitionerDocument> documentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> practitionerColumn;
    @FXML private TableColumn<PractitionerDocument, String> enrollmentColumn;
    @FXML private TableColumn<PractitionerDocument, String> nameColumn;
    @FXML private TableColumn<PractitionerDocument, String> statusColumn;
    @FXML private TableColumn<PractitionerDocument, String> dateColumn;

    private final PractitionerDocumentManager documentManager;
    private final ObservableList<PractitionerDocument> allDocuments = FXCollections.observableArrayList();

    private FilteredList<PractitionerDocument> filteredDocuments;

    @Inject
    public CoordinatorDocumentsController(PractitionerDocumentManager documentManager) {
        this.documentManager = documentManager;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadDocuments();
    }

    private void setupColumns() {
        practitionerColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPractitionerName()));
        enrollmentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPractitionerEnrollment()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getUploadDate())));
    }

    private void bindFilteredTable() {
        filteredDocuments = new FilteredList<>(allDocuments);
        documentsTableView.setItems(filteredDocuments);
    }

    private void loadDocuments() {
        try {
            allDocuments.setAll(documentManager.getAllDocuments());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadDocuments();
    }

    @FXML
    private void handleViewFileAction() {
        PractitionerDocument selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento para abrirlo.");
            return;
        }

        try {
            Desktop.getDesktop().browse(new URI(selectedDocument.getStoredFileUrl()));
        } catch (Exception e) {
            Controller.showErrorAlert("Error de archivo", FILE_OPEN_ERROR);
        }
    }

    @FXML
    private void handleMarkReviewedAction() {
        PractitionerDocument selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento para marcarlo como revisado.");
            return;
        }

        if (DocumentStatus.REVIEWED.getDatabaseValue().equalsIgnoreCase(selectedDocument.getStatus())) {
            Controller.showInfoAlert("Documento revisado", "Este documento ya está marcado como revisado.");
            return;
        }

        try {
            documentManager.markDocumentAsReviewed(selectedDocument.getDocumentId());
            loadDocuments();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo actualizar", e.getMessage());
        }
    }

    private void applyFilter() {
        filteredDocuments.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(PractitionerDocument document) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (safeText(document.getPractitionerName()) + " "
                + safeText(document.getPractitionerEnrollment())).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String safeText(String value) {
        return value != null ? value : "";
    }

    private String formatDate(Timestamp date) {
        return date != null ? date.toLocalDateTime().format(DATE_FORMATTER) : NO_DATE_LABEL;
    }
}
