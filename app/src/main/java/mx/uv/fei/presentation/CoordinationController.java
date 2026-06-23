package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.List;

@Component
public class CoordinationController {

    private static final String FORM_TITLE_REGISTER = "Registrar coordinador";
    private static final String FORM_TITLE_EDIT = "Editar coordinador";
    private static final String STATUS_FILTER_ALL = "Todos los estados";

    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private VBox activeCoordinatorCard;
    @FXML private Label activeCoordinatorNameLabel;
    @FXML private Label activeCoordinatorMetaLabel;
    @FXML private Label activeCoordinatorStatusLabel;
    @FXML private Label noActiveCoordinatorLabel;

    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Button refreshButton;

    @FXML private TableView<Coordinator> coordinatorsTableView;
    @FXML private TableColumn<Coordinator, String> nameColumn;
    @FXML private TableColumn<Coordinator, String> userNameColumn;
    @FXML private TableColumn<Coordinator, String> emailColumn;
    @FXML private TableColumn<Coordinator, String> statusColumn;

    @FXML private Button registerButton;
    @FXML private Button activateButton;

    @FXML private Label formTitleLabel;
    @FXML private FormField nameFormField;
    @FXML private FormField lastNameFormField;
    @FXML private FormField emailFormField;
    @FXML private FormField personalNumberFormField;
    @FXML private FormComboBox genderFormComboBox;

    private final CoordinatorManager coordinatorManager;
    private final ObservableList<Coordinator> allCoordinators = FXCollections.observableArrayList();

    private FilteredList<Coordinator> filteredCoordinators;
    private Coordinator coordinatorBeingEdited;

    @Inject
    public CoordinationController(CoordinatorManager coordinatorManager) {
        this.coordinatorManager = coordinatorManager;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        bindFilteredTable();
        setupGenderOptions();
        setupStatusFilterOptions();
        loadCoordinators();
        showListPane();
    }

    private void bindFilteredTable() {
        filteredCoordinators = new FilteredList<>(allCoordinators);
        coordinatorsTableView.setItems(filteredCoordinators);
    }

    private void setupStatusFilterOptions() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                STATUS_FILTER_ALL,
                UserStatus.ACTIVE.getDisplayLabel(),
                UserStatus.INACTIVE.getDisplayLabel(),
                UserStatus.PENDING.getDisplayLabel());
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
    }

    private void setupTableColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getName() + " " + data.getValue().getLastName()));
        userNameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void setupGenderOptions() {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        genderFormComboBox.setItems(genderOptions);
    }

    private void loadCoordinators() {
        try {
            List<Coordinator> coordinators = coordinatorManager.getAllCoordinators();
            allCoordinators.setAll(coordinators);
            applyFilters();

            Coordinator currentCoordinator = coordinatorManager.retrieveCurrentCoordinator();
            refreshActiveCoordinatorCard(currentCoordinator);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void applyFilters() {
        filteredCoordinators.setPredicate(this::matchesActiveFilters);
    }

    private boolean matchesActiveFilters(Coordinator coordinator) {
        return matchesSearchText(coordinator) && matchesStatusFilter(coordinator);
    }

    private boolean matchesSearchText(Coordinator coordinator) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (coordinator.getName() + " " + coordinator.getLastName() + " "
                + coordinator.getUserName() + " " + coordinator.getEmail()).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private boolean matchesStatusFilter(Coordinator coordinator) {
        String selectedStatus = statusFilterComboBox.getValue();
        boolean isMatch = selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus);

        if (!isMatch && coordinator.getStatus() != null) {
            isMatch = coordinator.getStatus().getDisplayLabel().equals(selectedStatus);
        }

        return isMatch;
    }

    private void refreshActiveCoordinatorCard(Coordinator currentCoordinator) {
        boolean hasActiveCoordinator = currentCoordinator != null;

        setNodeVisible(activeCoordinatorCard, hasActiveCoordinator);
        setNodeVisible(noActiveCoordinatorLabel, !hasActiveCoordinator);
        registerButton.setDisable(hasActiveCoordinator);
        activateButton.setDisable(hasActiveCoordinator);

        if (hasActiveCoordinator) {
            activeCoordinatorNameLabel.setText(
                    currentCoordinator.getName() + " " + currentCoordinator.getLastName());
            activeCoordinatorMetaLabel.setText(
                    currentCoordinator.getUserName() + "  ·  " + currentCoordinator.getEmail());
            activeCoordinatorStatusLabel.setText(currentCoordinator.getStatus().getDisplayLabel());
        }
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
        loadCoordinators();
    }

    @FXML
    private void handleInactivateAction() {
        try {
            Coordinator currentCoordinator = coordinatorManager.retrieveCurrentCoordinator();
            if (currentCoordinator == null) {
                Controller.showInfoAlert("Sin coordinador activo", "No hay un coordinador en turno para inactivar.");
                return;
            }
            coordinatorManager.inactivateCoordinator(currentCoordinator.getId());
            Controller.showSuccessAlert("Coordinador inactivado", "El coordinador en turno fue inactivado.");
            loadCoordinators();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    @FXML
    private void handleActivateSelectedAction() {
        Coordinator selectedCoordinator = coordinatorsTableView.getSelectionModel().getSelectedItem();
        if (selectedCoordinator == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un coordinador de la lista para activarlo.");
            return;
        }

        try {
            coordinatorManager.activateCoordinator(selectedCoordinator.getId());
            Controller.showSuccessAlert("Coordinador activado", "El coordinador fue activado correctamente.");
            loadCoordinators();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleEditSelectedAction() {
        Coordinator selectedCoordinator = coordinatorsTableView.getSelectionModel().getSelectedItem();
        if (selectedCoordinator == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un coordinador de la lista para editarlo.");
            return;
        }

        coordinatorBeingEdited = selectedCoordinator;
        prepareFormForEdit(selectedCoordinator);
        showFormPane();
    }

    @FXML
    private void handleShowRegisterFormAction() {
        coordinatorBeingEdited = null;
        prepareFormForCreate();
        showFormPane();
    }

    @FXML
    private void handleSaveCoordinatorAction() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        if (coordinatorBeingEdited != null) {
            saveEditedCoordinator();
        } else {
            saveNewCoordinator();
        }
    }

    @FXML
    private void handleCancelFormAction() {
        coordinatorBeingEdited = null;
        showListPane();
    }

    private void saveNewCoordinator() {
        try {
            Coordinator coordinator = buildCoordinatorFromForm();
            String temporaryPassword = coordinatorManager.registerNewCoordinator(coordinator);
            Controller.showSuccessAlert("Registro exitoso",
                    "El coordinador fue registrado correctamente.\nContraseña temporal: " + temporaryPassword);
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error en el registro", e.getMessage());
        }
    }

    private void saveEditedCoordinator() {
        try {
            applyEditableFields(coordinatorBeingEdited);
            coordinatorManager.updateCoordinator(coordinatorBeingEdited, coordinatorBeingEdited.getId());
            Controller.showSuccessAlert("Actualización exitosa",
                    "La información del coordinador se actualizó correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al actualizar", e.getMessage());
        }
    }

    private void returnToList() {
        coordinatorBeingEdited = null;
        loadCoordinators();
        showListPane();
    }

    private void prepareFormForCreate() {
        formTitleLabel.setText(FORM_TITLE_REGISTER);
        nameFormField.setText("");
        lastNameFormField.setText("");
        emailFormField.setText("");
        personalNumberFormField.setText("");
        personalNumberFormField.setDisable(false);
        genderFormComboBox.clearSelection();
    }

    private void prepareFormForEdit(Coordinator coordinator) {
        formTitleLabel.setText(FORM_TITLE_EDIT);
        nameFormField.setText(coordinator.getName());
        lastNameFormField.setText(coordinator.getLastName());
        emailFormField.setText(coordinator.getEmail());
        personalNumberFormField.setText(coordinator.getUserName());
        personalNumberFormField.setDisable(true);
        genderFormComboBox.valueProperty().set(coordinator.getGender().getDisplayValue());
    }

    private Coordinator buildCoordinatorFromForm() {
        Coordinator coordinator = new Coordinator();
        applyEditableFields(coordinator);
        coordinator.setUserName(personalNumberFormField.getText().trim());
        return coordinator;
    }

    private void applyEditableFields(Coordinator coordinator) {
        coordinator.setName(nameFormField.getText().trim());
        coordinator.setLastName(lastNameFormField.getText().trim());
        coordinator.setEmail(emailFormField.getText().trim());
        coordinator.setGender(Gender.fromDisplayValue(genderFormComboBox.getValue()));
    }

    private boolean isFormIncomplete() {
        return nameFormField.getText().isEmpty()
                || lastNameFormField.getText().isEmpty()
                || emailFormField.getText().isEmpty()
                || personalNumberFormField.getText().isEmpty()
                || genderFormComboBox.getValue() == null;
    }

    private void showListPane() {
        setNodeVisible(listPane, true);
        setNodeVisible(formPane, false);
    }

    private void showFormPane() {
        setNodeVisible(listPane, false);
        setNodeVisible(formPane, true);
    }

    private String statusLabelOf(Coordinator coordinator) {
        return coordinator.getStatus() != null ? coordinator.getStatus().getDisplayLabel() : "";
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
