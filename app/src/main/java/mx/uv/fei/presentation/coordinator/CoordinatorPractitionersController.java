package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoordinatorPractitionersController {

    private static final String NO_GROUP_LABEL = "Sin grupo";
    private static final String NO_STATUS_LABEL = "—";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerPractitioner.fxml";
    private static final String ENROLLMENT_FORM_VIEW = "/mx/uv/fei/presentation/registerEnrollment.fxml";

    @FXML private TextField searchTextField;
    @FXML private TableView<Practitioner> practitionersTableView;
    @FXML private TableColumn<Practitioner, String> nameColumn;
    @FXML private TableColumn<Practitioner, String> enrollmentColumn;
    @FXML private TableColumn<Practitioner, String> groupColumn;
    @FXML private TableColumn<Practitioner, String> statusColumn;

    private final PractitionerManager practitionerManager;
    private final PracticeGroupManager practiceGroupManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<Practitioner> allPractitioners = FXCollections.observableArrayList();
    private final Map<Integer, String> sectionByGroupId = new HashMap<>();

    private FilteredList<Practitioner> filteredPractitioners;

    @Inject
    public CoordinatorPractitionersController(PractitionerManager practitionerManager,
                                              PracticeGroupManager practiceGroupManager, ShellNavigator shellNavigator) {
        this.practitionerManager = practitionerManager;
        this.practiceGroupManager = practiceGroupManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadPractitioners();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getName() + " " + data.getValue().getLastName()));
        enrollmentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEnrollment()));
        groupColumn.setCellValueFactory(data -> new SimpleStringProperty(groupLabelOf(data.getValue())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredPractitioners = new FilteredList<>(allPractitioners);
        practitionersTableView.setItems(filteredPractitioners);
    }

    private void loadPractitioners() {
        try {
            mapGroupSections(practiceGroupManager.getAllPracticeGroups());
            allPractitioners.setAll(practitionerManager.getAllPractitioners());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void mapGroupSections(List<PracticeGroup> groups) {
        sectionByGroupId.clear();
        for (PracticeGroup group : groups) {
            sectionByGroupId.put(group.getGroupId(), group.getSection());
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadPractitioners();
    }

    @FXML
    private void handleActivateAction() {
        Practitioner selectedPractitioner = practitionersTableView.getSelectionModel().getSelectedItem();
        if (selectedPractitioner == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un practicante para activarlo.");
        } else {
            activateSelectedPractitioner(selectedPractitioner);
        }
    }

    private void activateSelectedPractitioner(Practitioner selectedPractitioner) {
        try {
            practitionerManager.activatePractitioner(selectedPractitioner.getId());
            loadPractitioners();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleInactivateAction() {
        Practitioner selectedPractitioner = practitionersTableView.getSelectionModel().getSelectedItem();
        if (selectedPractitioner == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un practicante para inactivarlo.");
        } else {
            inactivateSelectedPractitioner(selectedPractitioner);
        }
    }

    private void inactivateSelectedPractitioner(Practitioner selectedPractitioner) {
        try {
            practitionerManager.inactivatePractitioner(selectedPractitioner.getId());
            loadPractitioners();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    @FXML
    private void handleEditAction() {
        Practitioner selectedPractitioner = practitionersTableView.getSelectionModel().getSelectedItem();
        if (selectedPractitioner == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un practicante para editarlo.");
        } else {
            openForEdit(selectedPractitioner);
        }
    }

    private void openForEdit(Practitioner practitioner) {
        shellNavigator.openForm(REGISTER_FORM_VIEW, practitioner);
    }

    @FXML
    private void handleReEnrollAction() {
        Practitioner selectedPractitioner = practitionersTableView.getSelectionModel().getSelectedItem();
        if (selectedPractitioner == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un practicante para reinscribirlo.");
        } else {
            shellNavigator.openForm(ENROLLMENT_FORM_VIEW, selectedPractitioner);
        }
    }

    @FXML
    private void handleRegisterAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private void applyFilter() {
        filteredPractitioners.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Practitioner practitioner) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (practitioner.getName() + " " + practitioner.getLastName() + " "
                + practitioner.getEnrollment()).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String groupLabelOf(Practitioner practitioner) {
        return practitioner.getGroupId() != null
                ? sectionByGroupId.getOrDefault(practitioner.getGroupId(), NO_GROUP_LABEL)
                : NO_GROUP_LABEL;
    }

    private String statusLabelOf(Practitioner practitioner) {
        return practitioner.getStatus() != null ? practitioner.getStatus().getDisplayLabel() : NO_STATUS_LABEL;
    }
}
