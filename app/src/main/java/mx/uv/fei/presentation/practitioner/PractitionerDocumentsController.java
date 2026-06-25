package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.DocumentCategory;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.enums.DocumentType;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.reporting.MonthlyReportManager;
import mx.uv.fei.domain.manager.reporting.PractitionerDocumentManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

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
            "Tus documentos finales se habilitarán cuando tu profesor acepte todos tus documentos iniciales.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label initialLockedLabel;
    @FXML private HBox initialActionsBox;
    @FXML private ComboBox<DocumentType> initialTypeComboBox;
    @FXML private TableView<PractitionerDocument> initialDocumentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> initialTypeColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialNameColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialStatusColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialCommentColumn;
    @FXML private TableColumn<PractitionerDocument, String> initialDateColumn;

    @FXML private Label finalLockedLabel;
    @FXML private HBox finalActionsBox;
    @FXML private ComboBox<DocumentType> finalTypeComboBox;
    @FXML private TableView<PractitionerDocument> finalDocumentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> finalTypeColumn;
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
        setupTypeComboBox(initialTypeComboBox, DocumentCategory.INITIAL);
        setupTypeComboBox(finalTypeComboBox, DocumentCategory.FINAL);
        setupColumns(initialTypeColumn, initialNameColumn, initialStatusColumn, initialCommentColumn, initialDateColumn);
        setupColumns(finalTypeColumn, finalNameColumn, finalStatusColumn, finalCommentColumn, finalDateColumn);
        initialDocumentsTableView.setItems(initialDocuments);
        finalDocumentsTableView.setItems(finalDocuments);

        applyInitialTabAccess();
        applyFinalTabAccess();
    }

    private void setupTypeComboBox(ComboBox<DocumentType> comboBox, DocumentCategory category) {
        comboBox.setItems(FXCollections.observableArrayList(DocumentType.valuesForCategory(category)));
        comboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(DocumentType documentType) {
                return documentType != null ? documentType.getDisplayName() : "";
            }

            @Override
            public DocumentType fromString(String displayName) {
                return null;
            }
        });
    }

    private void setupColumns(TableColumn<PractitionerDocument, String> typeColumn,
                              TableColumn<PractitionerDocument, String> nameColumn,
                              TableColumn<PractitionerDocument, String> statusColumn,
                              TableColumn<PractitionerDocument, String> commentColumn,
                              TableColumn<PractitionerDocument, String> dateColumn) {
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentTypeName()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusDisplay(data.getValue().getStatus())));
        commentColumn.setCellValueFactory(data -> new SimpleStringProperty(safeComment(data.getValue().getReviewComment())));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getUploadDate())));
    }

    private void applyInitialTabAccess() {
        boolean hasProject = checkAccess(this::hasAssignedProject);
        toggleTabAccess(hasProject, initialLockedLabel, INITIAL_LOCKED_MESSAGE, initialActionsBox);
        if (hasProject) {
            loadDocuments(DocumentCategory.INITIAL, initialDocuments);
        }
    }

    private void applyFinalTabAccess() {
        boolean areInitialsAccepted = checkAccess(this::areInitialDocumentsAccepted);
        toggleTabAccess(areInitialsAccepted, finalLockedLabel, FINAL_LOCKED_MESSAGE, finalActionsBox);
        if (areInitialsAccepted) {
            loadDocuments(DocumentCategory.FINAL, finalDocuments);
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

    private boolean areInitialDocumentsAccepted() throws ManagerException {
        return documentManager.areAllInitialDocumentsAccepted(currentPractitionerId());
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

    private void loadDocuments(DocumentCategory category, ObservableList<PractitionerDocument> target) {
        try {
            target.setAll(documentManager.getPractitionerDocuments(currentPractitionerId(), category));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    @FXML
    private void handleUploadInitialAction() {
        uploadDocument(initialTypeComboBox, DocumentCategory.INITIAL, initialDocuments);
    }

    @FXML
    private void handleUploadFinalAction() {
        uploadDocument(finalTypeComboBox, DocumentCategory.FINAL, finalDocuments);
    }

    private void uploadDocument(ComboBox<DocumentType> typeComboBox, DocumentCategory category,
                                ObservableList<PractitionerDocument> target) {
        DocumentType selectedType = typeComboBox.getValue();
        File selectedFile = selectedType != null ? chooseFile() : null;
        if (selectedType == null) {
            Controller.showInfoAlert("Tipo requerido", "Selecciona el tipo de documento que vas a subir.");
        } else if (selectedFile != null) {
            persistUploadedDocument(selectedType, selectedFile, category, target);
        }
    }

    private void persistUploadedDocument(DocumentType selectedType, File selectedFile, DocumentCategory category,
                                         ObservableList<PractitionerDocument> target) {
        try {
            documentManager.uploadDocument(currentPractitionerId(), selectedType, selectedFile);
            Controller.showInfoAlert("Documento subido", "El documento se subió correctamente.");
            loadDocuments(category, target);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al subir", e.getMessage());
        }
    }

    @FXML
    private void handleEditInitialAction() {
        editDocument(initialDocumentsTableView, DocumentCategory.INITIAL, initialDocuments);
    }

    @FXML
    private void handleEditFinalAction() {
        editDocument(finalDocumentsTableView, DocumentCategory.FINAL, finalDocuments);
    }

    private void editDocument(TableView<PractitionerDocument> tableView, DocumentCategory category,
                              ObservableList<PractitionerDocument> target) {
        PractitionerDocument selectedDocument = tableView.getSelectionModel().getSelectedItem();
        File selectedFile = selectedDocument != null ? chooseFile() : null;
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento de la tabla para editarlo.");
        } else if (selectedFile != null) {
            persistEditedDocument(selectedDocument, selectedFile, category, target);
        }
    }

    private void persistEditedDocument(PractitionerDocument selectedDocument, File selectedFile,
                                       DocumentCategory category, ObservableList<PractitionerDocument> target) {
        try {
            documentManager.editDocument(selectedDocument.getDocumentId(), selectedFile);
            Controller.showInfoAlert("Documento actualizado", "El documento se actualizó y quedó pendiente de revisión.");
            loadDocuments(category, target);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al editar", e.getMessage());
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
        } else {
            Controller.openStoredFile(selectedDocument.getStoredFileUrl());
        }
    }

    private int currentPractitionerId() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        return currentUser != null ? currentUser.getId() : 0;
    }

    private String statusDisplay(String databaseStatus) {
        return databaseStatus != null ? DocumentStatus.fromString(databaseStatus).getDisplayName() : "";
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
