package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Coordinator;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.CoordinatorManager;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
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

    private static final String STATUS_FILTER_ALL = "Todos los estados";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerCoordinator.fxml";

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

    private final CoordinatorManager coordinatorManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<Coordinator> allCoordinators = FXCollections.observableArrayList();

    private FilteredList<Coordinator> filteredCoordinators;

    @Inject
    public CoordinationController(CoordinatorManager coordinatorManager, ShellNavigator shellNavigator) {
        this.coordinatorManager = coordinatorManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        bindFilteredTable();
        setupStatusFilterOptions();
        loadCoordinators();
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

        shellNavigator.openForm(REGISTER_FORM_VIEW, selectedCoordinator);
    }

    @FXML
    private void handleShowRegisterFormAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private String statusLabelOf(Coordinator coordinator) {
        return coordinator.getStatus() != null ? coordinator.getStatus().getDisplayLabel() : "";
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
