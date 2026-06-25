package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.enums.PeriodStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodConclusionManager;
import mx.uv.fei.domain.manager.academic.PeriodManager;
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

import java.sql.Date;

@Component
public class CoordinatorPeriodsController {

    private static final String STATUS_FILTER_ALL = "Todos los estados";
    private static final String NO_VALUE = "—";
    private static final String DATE_SEPARATOR = "  ·  ";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerPeriod.fxml";

    @FXML private VBox activePeriodCard;
    @FXML private Label activePeriodNameLabel;
    @FXML private Label activePeriodMetaLabel;
    @FXML private Label activePeriodStatusLabel;
    @FXML private Label noActivePeriodLabel;

    @FXML private TextField searchTextField;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private Button registerButton;
    @FXML private Button activateButton;

    @FXML private TableView<Period> periodsTableView;
    @FXML private TableColumn<Period, String> nameColumn;
    @FXML private TableColumn<Period, String> startColumn;
    @FXML private TableColumn<Period, String> endColumn;
    @FXML private TableColumn<Period, String> statusColumn;

    private final PeriodManager periodManager;
    private final PeriodConclusionManager periodConclusionManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<Period> allPeriods = FXCollections.observableArrayList();

    private FilteredList<Period> filteredPeriods;

    @Inject
    public CoordinatorPeriodsController(PeriodManager periodManager,
                                        PeriodConclusionManager periodConclusionManager,
                                        ShellNavigator shellNavigator) {
        this.periodManager = periodManager;
        this.periodConclusionManager = periodConclusionManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        setupStatusFilterOptions();
        loadPeriods();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPeriodName()));
        startColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getStartDate())));
        endColumn.setCellValueFactory(data -> new SimpleStringProperty(formatDate(data.getValue().getEndDate())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredPeriods = new FilteredList<>(allPeriods);
        periodsTableView.setItems(filteredPeriods);
    }

    private void setupStatusFilterOptions() {
        ObservableList<String> statusOptions = FXCollections.observableArrayList(
                STATUS_FILTER_ALL,
                PeriodStatus.UPCOMING.getDisplayLabel(),
                PeriodStatus.ACTIVE.getDisplayLabel(),
                PeriodStatus.CONCLUDED.getDisplayLabel());
        statusFilterComboBox.setItems(statusOptions);
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
    }

    private void loadPeriods() {
        try {
            allPeriods.setAll(periodManager.getAllPeriods());
            applyFilters();
            refreshActivePeriodCard(periodManager.getActivePeriod());
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void refreshActivePeriodCard(Period activePeriod) {
        boolean hasActivePeriod = activePeriod != null;

        setNodeVisible(activePeriodCard, hasActivePeriod);
        setNodeVisible(noActivePeriodLabel, !hasActivePeriod);
        registerButton.setDisable(hasActivePeriod);
        activateButton.setDisable(hasActivePeriod);

        if (hasActivePeriod) {
            activePeriodNameLabel.setText(activePeriod.getPeriodName());
            activePeriodMetaLabel.setText(formatDate(activePeriod.getStartDate())
                    + DATE_SEPARATOR + formatDate(activePeriod.getEndDate()));
            activePeriodStatusLabel.setText(statusLabelOf(activePeriod));
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
        loadPeriods();
    }

    @FXML
    private void handleActivateSelectedAction() {
        Period selectedPeriod = periodsTableView.getSelectionModel().getSelectedItem();
        if (selectedPeriod == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un periodo de la lista para activarlo.");
        } else {
            activateSelectedPeriod(selectedPeriod);
        }
    }

    private void activateSelectedPeriod(Period selectedPeriod) {
        try {
            periodManager.activatePeriod(selectedPeriod.getPeriodId());
            Controller.showSuccessAlert("Periodo activado", "El periodo fue activado correctamente.");
            loadPeriods();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleInactivateAction() {
        try {
            Period activePeriod = periodManager.getActivePeriod();
            if (activePeriod == null) {
                Controller.showInfoAlert("Sin periodo activo", "No hay un periodo activo para concluir.");
            } else {
                concludeActivePeriod(activePeriod);
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    private void concludeActivePeriod(Period activePeriod) throws ManagerException {
        periodConclusionManager.concludePeriod(activePeriod.getPeriodId());
        Controller.showSuccessAlert("Periodo concluido",
                "El periodo activo fue concluido. Los practicantes sin calificación recibieron 5 automáticamente.");
        loadPeriods();
    }

    @FXML
    private void handleEditSelectedAction() {
        Period selectedPeriod = periodsTableView.getSelectionModel().getSelectedItem();
        if (selectedPeriod == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un periodo de la lista para editarlo.");
        } else {
            shellNavigator.openForm(REGISTER_FORM_VIEW, selectedPeriod);
        }
    }

    @FXML
    private void handleRegisterAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private void applyFilters() {
        filteredPeriods.setPredicate(this::matchesActiveFilters);
    }

    private boolean matchesActiveFilters(Period period) {
        return matchesSearchText(period) && matchesStatusFilter(period);
    }

    private boolean matchesSearchText(Period period) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = period.getPeriodName() == null ? "" : period.getPeriodName().toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private boolean matchesStatusFilter(Period period) {
        String selectedStatus = statusFilterComboBox.getValue();
        boolean isMatch = selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus);

        if (!isMatch && period.getPeriodStatus() != null) {
            isMatch = statusLabelOf(period).equals(selectedStatus);
        }

        return isMatch;
    }

    private String statusLabelOf(Period period) {
        String label = NO_VALUE;

        if (period.getPeriodStatus() != null) {
            label = PeriodStatus.fromString(period.getPeriodStatus()).getDisplayLabel();
        }

        return label;
    }

    private String formatDate(Date date) {
        return date != null ? date.toString() : NO_VALUE;
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
