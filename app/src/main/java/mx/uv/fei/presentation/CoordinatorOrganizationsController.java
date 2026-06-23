package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.OrganizationManager;
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

@Component
public class CoordinatorOrganizationsController {

    private static final String STATUS_INACTIVE = "Inactive";
    private static final String STATUS_LABEL_ACTIVE = "Activo";
    private static final String STATUS_LABEL_INACTIVE = "Inactivo";
    private static final int DOUBLE_CLICK_COUNT = 2;

    @FXML private TextField searchTextField;
    @FXML private TableView<Organization> organizationsTableView;
    @FXML private TableColumn<Organization, String> nameColumn;
    @FXML private TableColumn<Organization, String> businessColumn;
    @FXML private TableColumn<Organization, String> emailColumn;
    @FXML private TableColumn<Organization, String> statusColumn;

    private final OrganizationManager organizationManager;
    private final AppStore store;
    private final ObservableList<Organization> allOrganizations = FXCollections.observableArrayList();

    private FilteredList<Organization> filteredOrganizations;

    @Inject
    public CoordinatorOrganizationsController(OrganizationManager organizationManager, AppStore store) {
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        setupTableInteraction();
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

    private void setupTableInteraction() {
        organizationsTableView.setOnMouseClicked(this::handleOrganizationRowClick);
    }

    private void handleOrganizationRowClick(MouseEvent event) {
        Organization selectedOrganization = organizationsTableView.getSelectionModel().getSelectedItem();
        if (event.getClickCount() == DOUBLE_CLICK_COUNT && selectedOrganization != null) {
            store.dispatch(new NavigationAction.ViewEntityDetails(
                    AppSection.REGISTER_ORGANIZATION, String.valueOf(selectedOrganization.getIdOrganization())));
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
    private void handleRegisterAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_ORGANIZATION));
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
