package mx.uv.fei.presentation.professor;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.*;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.domain.manager.academic.PostulationManager;
import mx.uv.fei.domain.manager.academic.PracticeGroupManager;
import mx.uv.fei.domain.manager.people.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class ProfessorPractitionersController {

    private static final String GROUP_FILTER_ALL = "Todos los grupos";
    private static final String NO_GROUP_LABEL = "Sin grupo";
    private static final String NO_PROJECT_LABEL = "Sin proyecto asignado";
    private static final String PRACTITIONER_DETAIL_VIEW = "/mx/uv/fei/presentation/practitionerDetail.fxml";

    @FXML private Label noActivePeriodLabel;
    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> groupFilterComboBox;

    @FXML private TableView<Practitioner> practitionersTableView;
    @FXML private TableColumn<Practitioner, String> nameColumn;
    @FXML private TableColumn<Practitioner, String> enrollmentColumn;
    @FXML private TableColumn<Practitioner, String> groupColumn;
    @FXML private TableColumn<Practitioner, String> projectColumn;

    private final PeriodManager periodManager;
    private final PracticeGroupManager practiceGroupManager;
    private final PractitionerManager practitionerManager;
    private final PostulationManager postulationManager;
    private final ShellNavigator shellNavigator;
    private final AppStore store;

    private final ObservableList<Practitioner> allPractitioners = FXCollections.observableArrayList();
    private final Map<Integer, String> sectionByPractitionerId = new HashMap<>();
    private final Map<Integer, String> projectNameByPractitionerId = new HashMap<>();

    private FilteredList<Practitioner> filteredPractitioners;
    private int professorId;
    private int activePeriodId;

    @Inject
    public ProfessorPractitionersController(
            PeriodManager periodManager,
            PracticeGroupManager practiceGroupManager,
            PractitionerManager practitionerManager,
            PostulationManager postulationManager,
            ShellNavigator shellNavigator,
            AppStore store
    ) {
        this.periodManager = periodManager;
        this.practiceGroupManager = practiceGroupManager;
        this.practitionerManager = practitionerManager;
        this.postulationManager = postulationManager;
        this.shellNavigator = shellNavigator;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        resolveProfessorContext();
        loadPractitioners();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getName() + " " + data.getValue().getLastName()));
        enrollmentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEnrollment()));
        groupColumn.setCellValueFactory(data -> new SimpleStringProperty(sectionOf(data.getValue())));
        projectColumn.setCellValueFactory(data -> new SimpleStringProperty(projectNameOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredPractitioners = new FilteredList<>(allPractitioners);
        practitionersTableView.setItems(filteredPractitioners);
    }

    private void resolveProfessorContext() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        professorId = currentUser != null ? currentUser.getId() : 0;

        try {
            Period activePeriod = periodManager.getActivePeriod();
            activePeriodId = activePeriod != null ? activePeriod.getPeriodId() : 0;
        } catch (ManagerException e) {
            activePeriodId = 0;
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void loadPractitioners() {
        boolean hasActivePeriod = activePeriodId > 0;
        setNodeVisible(noActivePeriodLabel, !hasActivePeriod);

        if (hasActivePeriod) {
            loadPractitionersForActivePeriod();
        }
    }

    private void loadPractitionersForActivePeriod() {
        clearCaches();
        ObservableList<String> groupOptions = FXCollections.observableArrayList(GROUP_FILTER_ALL);

        try {
            List<PracticeGroup> groups = practiceGroupManager.getGroupsByProfessorAndPeriod(professorId, activePeriodId);
            for (PracticeGroup group : groups) {
                groupOptions.add(group.getSection());
                loadGroupPractitioners(group);
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }

        groupFilterComboBox.setItems(groupOptions);
        groupFilterComboBox.setValue(GROUP_FILTER_ALL);
        applyFilters();
    }

    private void loadGroupPractitioners(PracticeGroup group) throws ManagerException {
        List<Practitioner> practitioners = practitionerManager.retrieveEnrolledPractitionersByGroup(group.getGroupId());
        for (Practitioner practitioner : practitioners) {
            practitioner.setGroupId(group.getGroupId());
            sectionByPractitionerId.put(practitioner.getId(), group.getSection());
            resolveAssignedProject(practitioner.getId());
            allPractitioners.add(practitioner);
        }
    }

    private void resolveAssignedProject(int practitionerId) throws ManagerException {
        Project assignedProject = postulationManager.getAssignedProject(practitionerId);
        if (assignedProject.getProjectId() > 0) {
            projectNameByPractitionerId.put(practitionerId, assignedProject.getProjectName());
        }
    }

    private void clearCaches() {
        allPractitioners.clear();
        sectionByPractitionerId.clear();
        projectNameByPractitionerId.clear();
    }

    @FXML
    private void handleSearchAction() {
        applyFilters();
    }

    @FXML
    private void handleFilterAction() {
        applyFilters();
    }

    @FXML
    private void handleRefreshAction() {
        resolveProfessorContext();
        loadPractitioners();
    }

    @FXML
    private void handleViewProjectAction() {
        Practitioner selectedPractitioner = practitionersTableView.getSelectionModel().getSelectedItem();
        if (selectedPractitioner == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un practicante para ver su detalle.");
        } else {
            shellNavigator.openForm(PRACTITIONER_DETAIL_VIEW, selectedPractitioner);
        }
    }

    private void applyFilters() {
        filteredPractitioners.setPredicate(this::matchesActiveFilters);
    }

    private boolean matchesActiveFilters(Practitioner practitioner) {
        return matchesSearchText(practitioner) && matchesGroupFilter(practitioner);
    }

    private boolean matchesSearchText(Practitioner practitioner) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (practitioner.getEnrollment() + " "
                + practitioner.getName() + " " + practitioner.getLastName()).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private boolean matchesGroupFilter(Practitioner practitioner) {
        String selectedGroup = groupFilterComboBox.getValue();
        boolean isMatch = selectedGroup == null || GROUP_FILTER_ALL.equals(selectedGroup);

        if (!isMatch) {
            isMatch = selectedGroup.equals(sectionByPractitionerId.get(practitioner.getId()));
        }

        return isMatch;
    }

    private String sectionOf(Practitioner practitioner) {
        return sectionByPractitionerId.getOrDefault(practitioner.getId(), NO_GROUP_LABEL);
    }

    private String projectNameOf(Practitioner practitioner) {
        return projectNameByPractitionerId.getOrDefault(practitioner.getId(), NO_PROJECT_LABEL);
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
