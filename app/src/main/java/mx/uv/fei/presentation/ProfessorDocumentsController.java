package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerDocumentManager;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class ProfessorDocumentsController {

    private static final String NO_DATE_LABEL = "—";
    private static final String ACCESS_ALREADY_GRANTED =
            "Este practicante ya tiene acceso al registro de reportes.";
    private static final String ACCESS_READY =
            "Todos los documentos iniciales están aceptados. Puedes otorgar el acceso a reportes.";
    private static final String ACCESS_PENDING =
            "Aún hay documentos iniciales sin aceptar. No puedes otorgar el acceso todavía.";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ListView<Practitioner> practitionersListView;
    @FXML private Label noPractitionersLabel;
    @FXML private VBox reviewContainer;
    @FXML private Label selectedPractitionerLabel;
    @FXML private TableView<PractitionerDocument> documentsTableView;
    @FXML private TableColumn<PractitionerDocument, String> typeColumn;
    @FXML private TableColumn<PractitionerDocument, String> nameColumn;
    @FXML private TableColumn<PractitionerDocument, String> statusColumn;
    @FXML private TableColumn<PractitionerDocument, String> dateColumn;
    @FXML private TextField rejectReasonTextField;
    @FXML private Label accessStatusLabel;
    @FXML private Button grantAccessButton;

    private final PractitionerDocumentManager documentManager;
    private final PractitionerManager practitionerManager;
    private final AppStore store;
    private final ObservableList<PractitionerDocument> documents = FXCollections.observableArrayList();

    private int professorId;
    private Practitioner selectedPractitioner;

    @Inject
    public ProfessorDocumentsController(PractitionerDocumentManager documentManager,
                                        PractitionerManager practitionerManager, AppStore store) {
        this.documentManager = documentManager;
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        showReviewContainer(false);
        setupColumns();
        documentsTableView.setItems(documents);
        configurePractitionerList();
        loadPractitioners();
    }

    private void setupColumns() {
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentType()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getStatus()));
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getUploadDate())));
    }

    private void configurePractitionerList() {
        practitionersListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Practitioner practitioner, boolean isEmpty) {
                super.updateItem(practitioner, isEmpty);
                if (isEmpty || practitioner == null) {
                    setText(null);
                } else {
                    setText(practitioner.getName() + " " + practitioner.getLastName()
                            + "  |  " + practitioner.getEnrollment());
                }
            }
        });

        practitionersListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selected) -> {
                    if (selected != null) {
                        showReviewForPractitioner(selected);
                    }
                });
    }

    private void loadPractitioners() {
        try {
            List<Practitioner> practitioners = practitionerManager.retrievePractitionersByProfessor(professorId);
            if (practitioners.isEmpty()) {
                noPractitionersLabel.setVisible(true);
                noPractitionersLabel.setManaged(true);
            } else {
                practitionersListView.setItems(FXCollections.observableArrayList(practitioners));
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void showReviewForPractitioner(Practitioner practitioner) {
        selectedPractitioner = practitioner;
        showReviewContainer(true);
        selectedPractitionerLabel.setText("Documentos de " + practitioner.getName() + " " + practitioner.getLastName());
        rejectReasonTextField.clear();
        loadDocumentsForSelected();
    }

    private void loadDocumentsForSelected() {
        try {
            List<PractitionerDocument> professorDocuments = documentManager.getDocumentsByProfessor(professorId);
            documents.setAll(professorDocuments.stream()
                    .filter(document -> document.getPractitionerId() == selectedPractitioner.getId())
                    .toList());
            updateAccessStatus();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void updateAccessStatus() {
        try {
            if (practitionerManager.isReportsAccessGranted(selectedPractitioner.getId())) {
                accessStatusLabel.setText(ACCESS_ALREADY_GRANTED);
                grantAccessButton.setDisable(true);
            } else if (documentManager.areAllInitialDocumentsAccepted(selectedPractitioner.getId())) {
                accessStatusLabel.setText(ACCESS_READY);
                grantAccessButton.setDisable(false);
            } else {
                accessStatusLabel.setText(ACCESS_PENDING);
                grantAccessButton.setDisable(true);
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de verificación", e.getMessage());
        }
    }

    @FXML
    private void handleViewFileAction() {
        PractitionerDocument selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento para abrirlo.");
            return;
        }
        Controller.openStoredFile(selectedDocument.getStoredFileUrl());
    }

    @FXML
    private void handleAcceptAction() {
        PractitionerDocument selectedDocument = requireSelectedDocument();
        if (selectedDocument == null) {
            return;
        }
        try {
            documentManager.acceptDocument(selectedDocument.getDocumentId());
            loadDocumentsForSelected();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo aceptar", e.getMessage());
        }
    }

    @FXML
    private void handleRejectAction() {
        PractitionerDocument selectedDocument = requireSelectedDocument();
        if (selectedDocument == null) {
            return;
        }
        try {
            documentManager.rejectDocument(selectedDocument.getDocumentId(), rejectReasonTextField.getText());
            rejectReasonTextField.clear();
            loadDocumentsForSelected();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo rechazar", e.getMessage());
        }
    }

    @FXML
    private void handleGrantAccessAction() {
        if (selectedPractitioner == null) {
            return;
        }
        try {
            if (!documentManager.areAllInitialDocumentsAccepted(selectedPractitioner.getId())) {
                Controller.showInfoAlert("Documentos pendientes", ACCESS_PENDING);
                return;
            }
            practitionerManager.grantReportsAccess(selectedPractitioner.getId());
            Controller.showInfoAlert("Acceso otorgado",
                    "El practicante ya puede registrar tareas y reportes.");
            updateAccessStatus();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo otorgar el acceso", e.getMessage());
        }
    }

    private PractitionerDocument requireSelectedDocument() {
        PractitionerDocument selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento de la tabla.");
        }
        return selectedDocument;
    }

    private void showReviewContainer(boolean isVisible) {
        reviewContainer.setVisible(isVisible);
        reviewContainer.setManaged(isVisible);
    }

    private String formatDate(Timestamp date) {
        return date != null ? date.toLocalDateTime().format(DATE_FORMATTER) : NO_DATE_LABEL;
    }
}
