package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.dto.Project;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.manager.ProjectManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoordinatorProjectsController {

    private static final String STATUS_INACTIVE = "Inactive";
    private static final String STATUS_LABEL_ACTIVE = "Activo";
    private static final String STATUS_LABEL_INACTIVE = "Inactivo";
    private static final String UNKNOWN_ORGANIZATION = "—";
    private static final int DOUBLE_CLICK_COUNT = 2;

    @FXML private TextField searchTextField;
    @FXML private TableView<Project> projectsTableView;
    @FXML private TableColumn<Project, String> nameColumn;
    @FXML private TableColumn<Project, String> organizationColumn;
    @FXML private TableColumn<Project, String> capacityColumn;
    @FXML private TableColumn<Project, String> statusColumn;

    private final ProjectManager projectManager;
    private final OrganizationManager organizationManager;
    private final AppStore store;
    private final ObservableList<Project> allProjects = FXCollections.observableArrayList();

    private FilteredList<Project> filteredProjects;
    private Map<Integer, String> organizationNamesById = new HashMap<>();

    @Inject
    public CoordinatorProjectsController(ProjectManager projectManager, OrganizationManager organizationManager,
            AppStore store) {
        this.projectManager = projectManager;
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        setupTableInteraction();
        loadProjects();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getProjectName()));
        organizationColumn.setCellValueFactory(data -> new SimpleStringProperty(organizationNameOf(data.getValue())));
        capacityColumn.setCellValueFactory(data -> new SimpleStringProperty(
                String.valueOf(data.getValue().getParticipantCapacity())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredProjects = new FilteredList<>(allProjects);
        projectsTableView.setItems(filteredProjects);
    }

    private void loadProjects() {
        try {
            organizationNamesById = mapOrganizationNames(organizationManager.getAllOrganizations());
            allProjects.setAll(projectManager.getAllProjects());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private Map<Integer, String> mapOrganizationNames(List<Organization> organizations) {
        Map<Integer, String> namesById = new HashMap<>();
        for (Organization organization : organizations) {
            namesById.put(organization.getIdOrganization(), organization.getNameOrganization());
        }

        return namesById;
    }

    private void setupTableInteraction() {
        projectsTableView.setOnMouseClicked(this::handleProjectRowClick);
    }

    private void handleProjectRowClick(MouseEvent event) {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (event.getClickCount() == DOUBLE_CLICK_COUNT && selectedProject != null) {
            openForEdit(selectedProject);
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadProjects();
    }

    @FXML
    private void handleActivateAction() {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un proyecto para activarlo.");
            return;
        }

        try {
            projectManager.activateProject(selectedProject.getProjectId());
            loadProjects();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleInactivateAction() {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un proyecto para inactivarlo.");
            return;
        }

        try {
            projectManager.inactivateProject(selectedProject.getProjectId());
            loadProjects();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    @FXML
    private void handleEditAction() {
        Project selectedProject = projectsTableView.getSelectionModel().getSelectedItem();
        if (selectedProject == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un proyecto para editarlo.");
            return;
        }

        openForEdit(selectedProject);
    }

    @FXML
    private void handleRegisterAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PROJECT));
    }

    private void openForEdit(Project project) {
        store.dispatch(new NavigationAction.ViewEntityDetails(
                AppSection.REGISTER_PROJECT, String.valueOf(project.getProjectId())));
    }

    private void applyFilter() {
        filteredProjects.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Project project) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (project.getProjectName() + " " + organizationNameOf(project)).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String organizationNameOf(Project project) {
        return organizationNamesById.getOrDefault(project.getCompanyId(), UNKNOWN_ORGANIZATION);
    }

    private String statusLabelOf(Project project) {
        String status = project.getStatus();
        return status != null && STATUS_INACTIVE.equalsIgnoreCase(status) ? STATUS_LABEL_INACTIVE : STATUS_LABEL_ACTIVE;
    }
}
