package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerDocumentManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.net.URI;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Component
public class PractitionerDocumentsController {

    private static final String UPLOAD_DIALOG_TITLE = "Seleccionar documento a subir";
    private static final String DOCUMENT_FILTER_LABEL = "Documentos";
    private static final String ALL_FILES_LABEL = "Todos los archivos";
    private static final String NO_DATE_LABEL = "—";
    private static final String FILE_OPEN_ERROR = "No se pudo abrir el documento.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private TableView<PractitionerDocument> documentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> nameColumn;
    @FXML private TableColumn<PractitionerDocument, String> statusColumn;
    @FXML private TableColumn<PractitionerDocument, String> dateColumn;

    private final PractitionerDocumentManager documentManager;
    private final AppStore store;
    private final ObservableList<PractitionerDocument> documents = FXCollections.observableArrayList();

    @Inject
    public PractitionerDocumentsController(PractitionerDocumentManager documentManager, AppStore store) {
        this.documentManager = documentManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns();
        documentsTableView.setItems(documents);
        loadDocuments();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getUploadDate())));
    }

    private void loadDocuments() {
        try {
            documents.setAll(documentManager.getPractitionerDocuments(currentPractitionerId()));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    @FXML
    private void handleUploadAction() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(UPLOAD_DIALOG_TITLE);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(DOCUMENT_FILTER_LABEL, "*.pdf", "*.doc", "*.docx", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter(ALL_FILES_LABEL, "*.*"));

        File selectedFile = fileChooser.showOpenDialog(documentsTableView.getScene().getWindow());
        if (selectedFile != null) {
            uploadSelectedDocument(selectedFile);
        }
    }

    private void uploadSelectedDocument(File selectedFile) {
        try {
            documentManager.uploadDocument(currentPractitionerId(), selectedFile);
            Controller.showInfoAlert("Documento subido", "El documento se subió correctamente.");
            loadDocuments();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al subir", e.getMessage());
        }
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
    private void handleRefreshAction() {
        loadDocuments();
    }

    private int currentPractitionerId() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        return currentUser != null ? currentUser.getId() : 0;
    }

    private String formatDate(Timestamp date) {
        return date != null ? date.toLocalDateTime().format(DATE_FORMATTER) : NO_DATE_LABEL;
    }
}
