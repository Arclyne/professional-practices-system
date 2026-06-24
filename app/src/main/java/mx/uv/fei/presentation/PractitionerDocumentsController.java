package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.DocumentType;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.manager.PractitionerDocumentManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;

@Component
public class PractitionerDocumentsController {

    private static final String UPLOAD_DIALOG_TITLE = "Seleccionar documento a subir";
    private static final String DOCUMENT_FILTER_LABEL = "Documentos";
    private static final String ALL_FILES_LABEL = "Todos los archivos";
    private static final String NO_DATE_LABEL = "—";
    private static final String NO_COMMENT_LABEL = "—";
    private static final String INITIAL_LOCKED_MESSAGE =
            "Esta sección estará disponible cuando tengas un proyecto asignado.";
    private static final String FINAL_LOCKED_MESSAGE =
            "Tus documentos finales se habilitarán cuando tu profesor apruebe tus documentos iniciales.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label initialLockedLabel;
    @FXML private HBox initialActionsBox;
    @FXML private TableView<PractitionerDocument> initialDocumentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> initialNameColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialStatusColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialCommentColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialDateColumn;

    @FXML private Label finalLockedLabel;
    @FXML private HBox finalActionsBox;
    @FXML private TableView<PractitionerDocument> finalDocumentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> finalNameColumn;
    @FXML private TableColumn<PractitionerDocument, String> finalStatusColumn;
    @FXML private TableColumn<PractitionerDocument, String> finalCommentColumn;
    @FXML private TableColumn<PractitionerDocument, String> finalDateColumn;

    private final PractitionerDocumentManager documentManager;
    private final MonthlyReportManager reportManager;
    private final AppStore store;
    private final ObservableList<PractitionerDocument> initialDocuments = FXCollections.observableArrayList();
    private final ObservableList<PractitionerDocument> finalDocuments = FXCollections.observableArrayList();

    @Inject
    public PractitionerDocumentsController(PractitionerDocumentManager documentManager,
                                           MonthlyReportManager reportManager, AppStore store) {
        this.documentManager = documentManager;
        this.reportManager = reportManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns(initialNameColumn, initialStatusColumn, initialCommentColumn, initialDateColumn);
        setupColumns(finalNameColumn, finalStatusColumn, finalCommentColumn, finalDateColumn);
        initialDocumentsTableView.setItems(initialDocuments);
        finalDocumentsTableView.setItems(finalDocuments);

        applyInitialTabAccess();
        applyFinalTabAccess();
    }

    private void setupColumns(TableColumn<PractitionerDocument, String> nameColumn,
                              TableColumn<PractitionerDocument, String> statusColumn,
                              TableColumn<PractitionerDocument, String> commentColumn,
                              TableColumn<PractitionerDocument, String> dateColumn) {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        commentColumn.setCellValueFactory(data -> new SimpleStringProperty(safeComment(data.getValue().getReviewComment())));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getUploadDate())));
    }

    private void applyInitialTabAccess() {
        boolean hasProject = checkAccess(this::hasAssignedProject);
        toggleTabAccess(hasProject, initialLockedLabel, INITIAL_LOCKED_MESSAGE, initialActionsBox);
        if (hasProject) {
            loadDocuments(DocumentType.INITIAL, initialDocuments);
        }
    }

    private void applyFinalTabAccess() {
        boolean isAccessGranted = checkAccess(this::hasReportsAccess);
        toggleTabAccess(isAccessGranted, finalLockedLabel, FINAL_LOCKED_MESSAGE, finalActionsBox);
        if (isAccessGranted) {
            loadDocuments(DocumentType.FINAL, finalDocuments);
        }
    }

    private void toggleTabAccess(boolean isUnlocked, Label lockedLabel, String lockedMessage, HBox actionsBox) {
        lockedLabel.setText(lockedMessage);
        lockedLabel.setVisible(!isUnlocked);
        lockedLabel.setManaged(!isUnlocked);
        actionsBox.setVisible(isUnlocked);
        actionsBox.setManaged(isUnlocked);
    }

    private boolean hasAssignedProject() throws ManagerException {
        return reportManager.verifyHasAssignedProject(currentPractitionerId());
    }

    private boolean hasReportsAccess() throws ManagerException {
        return reportManager.verifyReportsAccessGranted(currentPractitionerId());
    }

    private boolean checkAccess(AccessCheck accessCheck) {
        boolean isAllowed = false;
        try {
            isAllowed = accessCheck.isAllowed();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de verificación", e.getMessage());
        }
        return isAllowed;
    }

    private void loadDocuments(DocumentType documentType, ObservableList<PractitionerDocument> target) {
        try {
            target.setAll(documentManager.getPractitionerDocuments(currentPractitionerId(), documentType));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    @FXML
    private void handleUploadInitialAction() {
        uploadDocument(DocumentType.INITIAL, initialDocuments);
    }

    @FXML
    private void handleUploadFinalAction() {
        uploadDocument(DocumentType.FINAL, finalDocuments);
    }

    private void uploadDocument(DocumentType documentType, ObservableList<PractitionerDocument> target) {
        File selectedFile = chooseFile();
        if (selectedFile == null) {
            return;
        }
        try {
            documentManager.uploadDocument(currentPractitionerId(), selectedFile, documentType);
            Controller.showInfoAlert("Documento subido", "El documento se subió correctamente.");
            loadDocuments(documentType, target);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al subir", e.getMessage());
        }
    }

    private File chooseFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(UPLOAD_DIALOG_TITLE);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter(DOCUMENT_FILTER_LABEL, "*.pdf", "*.doc", "*.docx", "*.jpg", "*.jpeg", "*.png"),
                new FileChooser.ExtensionFilter(ALL_FILES_LABEL, "*.*"));
        return fileChooser.showOpenDialog(initialDocumentsTableView.getScene().getWindow());
    }

    @FXML
    private void handleViewInitialFileAction() {
        openSelectedFile(initialDocumentsTableView.getSelectionModel().getSelectedItem());
    }

    @FXML
    private void handleViewFinalFileAction() {
        openSelectedFile(finalDocumentsTableView.getSelectionModel().getSelectedItem());
    }

    private void openSelectedFile(PractitionerDocument selectedDocument) {
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento para abrirlo.");
            return;
        }
        Controller.openStoredFile(selectedDocument.getStoredFileUrl());
    }

    private int currentPractitionerId() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        return currentUser != null ? currentUser.getId() : 0;
    }

    private String safeComment(String comment) {
        return comment != null && !comment.isBlank() ? comment : NO_COMMENT_LABEL;
    }

    private String formatDate(Timestamp date) {
        return date != null ? date.toLocalDateTime().format(DATE_FORMATTER) : NO_DATE_LABEL;
    }

    @FunctionalInterface
    private interface AccessCheck {
        boolean isAllowed() throws ManagerException;
    }
}
