package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ManagerManager;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoordinatorManagersController {

    private static final String FORM_TITLE_REGISTER = "Registrar encargado";
    private static final String FORM_TITLE_EDIT = "Editar encargado";
    private static final String UNKNOWN_ORGANIZATION = "—";

    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TextField searchTextField;
    @FXML private TableView<Manager> managersTableView;
    @FXML private TableColumn<Manager, String> nameColumn;
    @FXML private TableColumn<Manager, String> phoneColumn;
    @FXML private TableColumn<Manager, String> emailColumn;
    @FXML private TableColumn<Manager, String> organizationColumn;
    @FXML private TableColumn<Manager, String> statusColumn;

    @FXML private Label formTitleLabel;
    @FXML private FormField nameFormField;
    @FXML private FormField phoneFormField;
    @FXML private FormField emailFormField;
    @FXML private FormComboBox organizationFormComboBox;

    private final ManagerManager managerManager;
    private final OrganizationManager organizationManager;
    private final ObservableList<Manager> allManagers = FXCollections.observableArrayList();

    private FilteredList<Manager> filteredManagers;
    private Manager managerBeingEdited;
    private Map<Integer, String> organizationNamesById = new HashMap<>();
    private Map<String, Integer> organizationIdsByName = new HashMap<>();

    @Inject
    public CoordinatorManagersController(ManagerManager managerManager, OrganizationManager organizationManager) {
        this.managerManager = managerManager;
        this.organizationManager = organizationManager;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadManagers();
        showListPane();
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
            List<Organization> organizations = organizationManager.getAllOrganizations();
            mapOrganizations(organizations);
            organizationFormComboBox.setItems(FXCollections.observableArrayList(organizationIdsByName.keySet()));
            allManagers.setAll(managerManager.getAllManagers());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void mapOrganizations(List<Organization> organizations) {
        organizationNamesById = new HashMap<>();
        organizationIdsByName = new HashMap<>();
        for (Organization organization : organizations) {
            organizationNamesById.put(organization.getIdOrganization(), organization.getNameOrganization());
            organizationIdsByName.put(organization.getNameOrganization(), organization.getIdOrganization());
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

        managerBeingEdited = selectedManager;
        prepareFormForEdit(selectedManager);
        showFormPane();
    }

    @FXML
    private void handleShowRegisterFormAction() {
        managerBeingEdited = null;
        prepareFormForCreate();
        showFormPane();
    }

    @FXML
    private void handleSaveAction() {
        if (isFormIncomplete()) {
            Controller.showInfoAlert("Campos incompletos", "Por favor, completa todos los datos del encargado.");
            return;
        }

        if (managerBeingEdited != null) {
            saveEditedManager();
        } else {
            saveNewManager();
        }
    }

    @FXML
    private void handleCancelFormAction() {
        managerBeingEdited = null;
        showListPane();
    }

    private void saveNewManager() {
        try {
            Manager manager = buildManagerFromForm();
            manager.setStatus(UserStatus.ACTIVE);
            managerManager.registerManager(manager);
            Controller.showSuccessAlert("Registro exitoso", "El encargado fue registrado correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error en el registro", e.getMessage());
        }
    }

    private void saveEditedManager() {
        try {
            Manager manager = buildManagerFromForm();
            managerManager.updateManager(manager, managerBeingEdited.getId());
            Controller.showSuccessAlert("Actualización exitosa", "El encargado se actualizó correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al actualizar", e.getMessage());
        }
    }

    private void returnToList() {
        managerBeingEdited = null;
        loadManagers();
        showListPane();
    }

    private Manager buildManagerFromForm() {
        Manager manager = new Manager();
        manager.setName(nameFormField.getText().trim());
        manager.setPhone(phoneFormField.getText().trim());
        manager.setEmail(emailFormField.getText().trim());
        manager.setOrganizationId(organizationIdsByName.getOrDefault(organizationFormComboBox.getValue(), 0));

        return manager;
    }

    private void prepareFormForCreate() {
        formTitleLabel.setText(FORM_TITLE_REGISTER);
        nameFormField.setText("");
        phoneFormField.setText("");
        emailFormField.setText("");
        organizationFormComboBox.clearSelection();
    }

    private void prepareFormForEdit(Manager manager) {
        formTitleLabel.setText(FORM_TITLE_EDIT);
        nameFormField.setText(manager.getName());
        phoneFormField.setText(manager.getPhone());
        emailFormField.setText(manager.getEmail());
        organizationFormComboBox.valueProperty().set(organizationNameOf(manager));
    }

    private boolean isFormIncomplete() {
        return nameFormField.getText().isEmpty()
                || phoneFormField.getText().isEmpty()
                || emailFormField.getText().isEmpty()
                || organizationFormComboBox.getValue() == null;
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

    private void showListPane() {
        setNodeVisible(listPane, true);
        setNodeVisible(formPane, false);
    }

    private void showFormPane() {
        setNodeVisible(listPane, false);
        setNodeVisible(formPane, true);
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
