package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.sql.Date;

@Component
public class CoordinatorPeriodsController {

    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_CONCLUDED = "Concluded";
    private static final String STATUS_UPCOMING = "Upcoming";
    private static final String STATUS_LABEL_ACTIVE = "Activo";
    private static final String STATUS_LABEL_CONCLUDED = "Concluido";
    private static final String STATUS_LABEL_UPCOMING = "Próximo";
    private static final String NO_VALUE = "—";
    private static final String REGISTER_FORM_VIEW = "/mx/uv/fei/presentation/registerPeriod.fxml";

    @FXML private TextField searchTextField;
    @FXML private TableView<Period> periodsTableView;
    @FXML private TableColumn<Period, String> nameColumn;
    @FXML private TableColumn<Period, String> startColumn;
    @FXML private TableColumn<Period, String> endColumn;
    @FXML private TableColumn<Period, String> statusColumn;

    private final PeriodManager periodManager;
    private final ShellNavigator shellNavigator;
    private final ObservableList<Period> allPeriods = FXCollections.observableArrayList();

    private FilteredList<Period> filteredPeriods;

    @Inject
    public CoordinatorPeriodsController(PeriodManager periodManager, ShellNavigator shellNavigator) {
        this.periodManager = periodManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
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

    private void loadPeriods() {
        try {
            allPeriods.setAll(periodManager.getAllPeriods());
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
        loadPeriods();
    }

    @FXML
    private void handleActivateAction() {
        Period selectedPeriod = periodsTableView.getSelectionModel().getSelectedItem();
        if (selectedPeriod == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un periodo de la lista para activarlo.");
            return;
        }

        try {
            periodManager.activatePeriod(selectedPeriod.getPeriodId());
            loadPeriods();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo activar", e.getMessage());
        }
    }

    @FXML
    private void handleInactivateAction() {
        Period selectedPeriod = periodsTableView.getSelectionModel().getSelectedItem();
        if (selectedPeriod == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un periodo de la lista para inactivarlo.");
            return;
        }

        try {
            periodManager.inactivatePeriod(selectedPeriod.getPeriodId());
            loadPeriods();
        } catch (ManagerException e) {
            Controller.showErrorAlert("No se pudo inactivar", e.getMessage());
        }
    }

    @FXML
    private void handleEditAction() {
        Period selectedPeriod = periodsTableView.getSelectionModel().getSelectedItem();
        if (selectedPeriod == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un periodo de la lista para editarlo.");
            return;
        }

        shellNavigator.openForm(REGISTER_FORM_VIEW, selectedPeriod);
    }

    @FXML
    private void handleRegisterAction() {
        shellNavigator.openForm(REGISTER_FORM_VIEW);
    }

    private void applyFilter() {
        filteredPeriods.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Period period) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = period.getPeriodName() == null ? "" : period.getPeriodName().toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String statusLabelOf(Period period) {
        String status = period.getPeriodStatus();
        String label = NO_VALUE;

        if (STATUS_ACTIVE.equalsIgnoreCase(status)) {
            label = STATUS_LABEL_ACTIVE;
        } else if (STATUS_CONCLUDED.equalsIgnoreCase(status)) {
            label = STATUS_LABEL_CONCLUDED;
        } else if (STATUS_UPCOMING.equalsIgnoreCase(status)) {
            label = STATUS_LABEL_UPCOMING;
        }

        return label;
    }

    private String formatDate(Date date) {
        return date != null ? date.toString() : NO_VALUE;
    }
}
