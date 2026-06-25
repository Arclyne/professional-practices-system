package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
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

@Component
public class CoordinatorOrganizationsController {

    private static final String STATUS_INACTIVE = "Inactive";
    private static final String STATUS_LABEL_ACTIVE = "Activo";
    private static final String STATUS_LABEL_INACTIVE = "Inactivo";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerOrganization.fxml";

    @FXML private TextField searchTextField;
    @FXML private TableView<Organization> organizationsTableView;
    @FXML private TableColumn<Organization, String> nameColumn;
    @FXML private TableColumn<Organization, String> businessColumn;
    @FXML private TableColumn<Organization, String> emailColumn;
    @FXML private TableColumn<Organization, String> statusColumn;

    private final OrganizationManager organizationManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<Organization> allOrganizations = FXCollections.observableArrayList();

    private FilteredList<Organization> filteredOrganizations;

    @Inject
    public CoordinatorOrganizationsController(OrganizationManager organizationManager, ShellNavigator shellNavigator) {
        this.organizationManager = organizationManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadOrganizations();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNameOrganization()));
        businessColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getBusiness()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMail()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredOrganizations = new FilteredList<>(allOrganizations);
        organizationsTableView.setItems(filteredOrganizations);
    }

    private void loadOrganizations() {
        try {
            allOrganizations.setAll(organizationManager.getAllOrganizations());
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
        loadOrganizations();
    }

    @FXML
    private void handleActivateAction() {
        Organization selectedOrganization = organizationsTableView.getSelectionModel().getSelectedItem();
        if (selectedOrganization == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona una organización para activarla.");
            return;
        }

        try {
            organizationManager.activateOrganization(selectedOrganization.getIdOrganization());
            loadOrganizations();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleInactivateAction() {
        Organization selectedOrganization = organizationsTableView.getSelectionModel().getSelectedItem();
        if (selectedOrganization == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona una organización para inactivarla.");
            return;
        }

        try {
            organizationManager.inactivateOrganization(selectedOrganization.getIdOrganization());
            loadOrganizations();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    @FXML
    private void handleEditAction() {
        Organization selectedOrganization = organizationsTableView.getSelectionModel().getSelectedItem();
        if (selectedOrganization == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona una organización para editarla.");
            return;
        }

        openForEdit(selectedOrganization);
    }

    @FXML
    private void handleRegisterAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private void openForEdit(Organization organization) {
        shellNavigator.openForm(REGISTER_FORM_VIEW, organization);
    }

    private void applyFilter() {
        filteredOrganizations.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Organization organization) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (organization.getNameOrganization() + " " + organization.getBusiness() + " "
                + organization.getMail()).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String statusLabelOf(Organization organization) {
        String status = organization.getState();
        return status != null && STATUS_INACTIVE.equalsIgnoreCase(status) ? STATUS_LABEL_INACTIVE : STATUS_LABEL_ACTIVE;
    }
}
