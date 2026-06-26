package mx.uv.fei.presentation.professor;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.PractitionerDocument;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.DocumentStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.reporting.PractitionerDocumentManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfessorDocumentsController {

    private static final String NO_DATE_LABEL = "—";
    private static final String ALL_GROUPS_OPTION = "Todos mis grupos";
    private static final String GROUP_LABEL_PREFIX = "NRC ";
    private static final String STATUS_FILTER_ALL = "Todos los estados";
    private static final String STATUS_FILTER_PENDING_REVIEW = "Por revisar (pendientes)";
    private static final String STATUS_FILTER_REJECTED = "Con rechazados";
    private static final String STATUS_FILTER_ALL_ACCEPTED = "Todo aceptado";
    private static final String STATUS_FILTER_NO_SUBMISSIONS = "Sin entregas";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private ComboBox<String> groupFilterComboBox;
    @FXML private ComboBox<String> statusFilterComboBox;
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

    private final PractitionerDocumentManager documentManager;
    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final AppStore store;
    private final ObservableList<PractitionerDocument> documents = FXCollections.observableArrayList();
    private final Map<String, Integer> groupLabelToId = new HashMap<>();
    private final Map<Integer, String> documentBucketByPractitionerId = new HashMap<>();

    private int professorId;
    private int activePeriodId;
    private Practitioner selectedPractitioner;

    @Inject
    public ProfessorDocumentsController(PractitionerDocumentManager documentManager,
                                        PractitionerManager practitionerManager,
                                        PracticeGroupManager practiceGroupManager, PeriodManager periodManager,
                                        AppStore store) {
        this.documentManager = documentManager;
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
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
        resolveActivePeriod();
        loadStatusFilter();
        loadProfessorGroups();
        loadPractitioners();
    }

    private void loadStatusFilter() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                STATUS_FILTER_ALL, STATUS_FILTER_PENDING_REVIEW, STATUS_FILTER_REJECTED,
                STATUS_FILTER_ALL_ACCEPTED, STATUS_FILTER_NO_SUBMISSIONS));
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> loadPractitioners());
    }

    private void setupColumns() {
        typeColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentTypeName()));
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDocumentName()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusDisplay(data.getValue().getStatus())));
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

        groupFilterComboBox.valueProperty().addListener((_, _, _) -> loadPractitioners());
    }

    private void resolveActivePeriod() {
        try {
            Period activePeriod = periodManager.getActivePeriod();
            activePeriodId = activePeriod != null ? activePeriod.getPeriodId() : 0;
        } catch (ManagerException e) {
            activePeriodId = 0;
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void loadProfessorGroups() {
        ObservableList<String> groupOptions = FXCollections.observableArrayList(ALL_GROUPS_OPTION);

        try {
            List<PracticeGroup> groups = practiceGroupManager.getGroupsByProfessorAndPeriod(professorId, activePeriodId);
            for (PracticeGroup group : groups) {
                String groupLabel = GROUP_LABEL_PREFIX + group.getSection();
                groupOptions.add(groupLabel);
                groupLabelToId.put(groupLabel, group.getGroupId());
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }

        groupFilterComboBox.setItems(groupOptions);
        groupFilterComboBox.setValue(ALL_GROUPS_OPTION);
    }

    private void loadPractitioners() {
        try {
            loadDocumentStatuses();
            List<Practitioner> practitioners = filterByDocumentStatus(retrievePractitionersForSelectedFilter());
            boolean hasPractitioners = !practitioners.isEmpty();

            practitionersListView.setItems(FXCollections.observableArrayList(practitioners));
            noPractitionersLabel.setVisible(!hasPractitioners);
            noPractitionersLabel.setManaged(!hasPractitioners);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void loadDocumentStatuses() throws ManagerException {
        documentBucketByPractitionerId.clear();
        Map<Integer, List<PractitionerDocument>> documentsByPractitioner = new HashMap<>();
        for (PractitionerDocument document : documentManager.getDocumentsByProfessor(professorId)) {
            documentsByPractitioner.computeIfAbsent(document.getPractitionerId(), _ -> new ArrayList<>()).add(document);
        }
        for (Map.Entry<Integer, List<PractitionerDocument>> entry : documentsByPractitioner.entrySet()) {
            documentBucketByPractitionerId.put(entry.getKey(), resolveDocumentBucket(entry.getValue()));
        }
    }

    private String resolveDocumentBucket(List<PractitionerDocument> practitionerDocuments) {
        boolean hasPending = practitionerDocuments.stream().anyMatch(this::isPending);
        boolean hasRejected = practitionerDocuments.stream().anyMatch(this::isRejected);
        String bucket;

        if (hasPending) {
            bucket = STATUS_FILTER_PENDING_REVIEW;
        } else if (hasRejected) {
            bucket = STATUS_FILTER_REJECTED;
        } else {
            bucket = STATUS_FILTER_ALL_ACCEPTED;
        }

        return bucket;
    }

    private boolean isPending(PractitionerDocument document) {
        return DocumentStatus.PENDING.getDatabaseValue().equals(document.getStatus());
    }

    private boolean isRejected(PractitionerDocument document) {
        return DocumentStatus.REJECTED.getDatabaseValue().equals(document.getStatus());
    }

    private List<Practitioner> filterByDocumentStatus(List<Practitioner> practitioners) {
        String selectedStatus = statusFilterComboBox.getValue();
        boolean includeAll = selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus);
        List<Practitioner> filtered = new ArrayList<>();

        for (Practitioner practitioner : practitioners) {
            String bucket = documentBucketByPractitionerId.getOrDefault(practitioner.getId(), STATUS_FILTER_NO_SUBMISSIONS);
            if (includeAll || selectedStatus.equals(bucket)) {
                filtered.add(practitioner);
            }
        }

        return filtered;
    }

    private List<Practitioner> retrievePractitionersForSelectedFilter() throws ManagerException {
        String selectedGroup = groupFilterComboBox.getValue();
        List<Practitioner> practitioners;

        if (selectedGroup == null || ALL_GROUPS_OPTION.equals(selectedGroup)) {
            practitioners = retrievePractitionersFromAllGroups();
        } else {
            practitioners = practitionerManager.retrieveEnrolledPractitionersByGroup(groupLabelToId.get(selectedGroup));
        }

        return practitioners;
    }

    private List<Practitioner> retrievePractitionersFromAllGroups() throws ManagerException {
        List<Practitioner> practitioners = new ArrayList<>();

        for (int groupId : groupLabelToId.values()) {
            practitioners.addAll(practitionerManager.retrieveEnrolledPractitionersByGroup(groupId));
        }

        return practitioners;
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
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    @FXML
    private void handleViewFileAction() {
        PractitionerDocument selectedDocument = documentsTableView.getSelectionModel().getSelectedItem();
        if (selectedDocument == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un documento para abrirlo.");
        } else {
            Controller.openStoredFile(selectedDocument.getStoredFileUrl());
        }
    }

    @FXML
    private void handleAcceptAction() {
        PractitionerDocument selectedDocument = requireSelectedDocument();
        if (selectedDocument != null) {
            acceptDocument(selectedDocument);
        }
    }

    private void acceptDocument(PractitionerDocument selectedDocument) {
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
        if (selectedDocument != null) {
            rejectDocument(selectedDocument);
        }
    }

    private void rejectDocument(PractitionerDocument selectedDocument) {
        try {
            documentManager.rejectDocument(selectedDocument.getDocumentId(), rejectReasonTextField.getText());
            rejectReasonTextField.clear();
            loadDocumentsForSelected();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo rechazar", e.getMessage());
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

    private String statusDisplay(String databaseStatus) {
        return databaseStatus != null ? DocumentStatus.fromString(databaseStatus).getDisplayName() : "";
    }

    private String formatDate(Timestamp date) {
        return date != null ? date.toLocalDateTime().format(DATE_FORMATTER) : NO_DATE_LABEL;
    }
}
