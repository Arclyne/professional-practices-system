package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.ManagerManager;
import mx.uv.fei.domain.manager.people.OrganizationManager;
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
public class CoordinatorManagersController {

    private static final String UNKNOWN_ORGANIZATION = "—";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerManager.fxml";

    @FXML private TextField searchTextField;
    @FXML private TableView<Manager> managersTableView;
    @FXML private TableColumn<Manager, String> nameColumn;
    @FXML private TableColumn<Manager, String> phoneColumn;
    @FXML private TableColumn<Manager, String> emailColumn;
    @FXML private TableColumn<Manager, String> organizationColumn;
    @FXML private TableColumn<Manager, String> statusColumn;

    private final ManagerManager managerManager;
    private final OrganizationManager organizationManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<Manager> allManagers = FXCollections.observableArrayList();

    private FilteredList<Manager> filteredManagers;
    private Map<Integer, String> organizationNamesById = new HashMap<>();

    @Inject
    public CoordinatorManagersController(ManagerManager managerManager, OrganizationManager organizationManager,
                                         ShellNavigator shellNavigator) {
        this.managerManager = managerManager;
        this.organizationManager = organizationManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadManagers();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getName()));
        phoneColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        organizationColumn.setCellValueFactory(data -> new SimpleStringProperty(organizationNameOf(data.getValue())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredManagers = new FilteredList<>(allManagers);
        managersTableView.setItems(filteredManagers);
    }

    private void loadManagers() {
        try {
            mapOrganizations(organizationManager.getAllOrganizations());
            allManagers.setAll(managerManager.getAllManagers());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void mapOrganizations(List<Organization> organizations) {
        organizationNamesById = new HashMap<>();
        for (Organization organization : organizations) {
            organizationNamesById.put(organization.getIdOrganization(), organization.getNameOrganization());
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadManagers();
    }

    @FXML
    private void handleActivateAction() {
        Manager selectedManager = managersTableView.getSelectionModel().getSelectedItem();
        if (selectedManager == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un encargado para activarlo.");
            return;
        }

        try {
            managerManager.activateManager(selectedManager.getId());
            loadManagers();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleInactivateAction() {
        Manager selectedManager = managersTableView.getSelectionModel().getSelectedItem();
        if (selectedManager == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un encargado para inactivarlo.");
            return;
        }

        try {
            managerManager.inactivateManager(selectedManager.getId());
            loadManagers();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    @FXML
    private void handleEditAction() {
        Manager selectedManager = managersTableView.getSelectionModel().getSelectedItem();
        if (selectedManager == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un encargado para editarlo.");
            return;
        }

        shellNavigator.openForm(REGISTER_FORM_VIEW, selectedManager);
    }

    @FXML
    private void handleRegisterAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private void applyFilter() {
        filteredManagers.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Manager manager) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (manager.getName() + " " + manager.getEmail() + " "
                + organizationNameOf(manager)).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String organizationNameOf(Manager manager) {
        return organizationNamesById.getOrDefault(manager.getOrganizationId(), UNKNOWN_ORGANIZATION);
    }

    private String statusLabelOf(Manager manager) {
        return manager.getStatus() != null ? manager.getStatus().getDisplayLabel() : UNKNOWN_ORGANIZATION;
    }
}
