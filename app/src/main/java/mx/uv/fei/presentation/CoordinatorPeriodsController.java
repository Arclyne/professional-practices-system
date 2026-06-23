package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.presentation.components.FormField;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Component
public class CoordinatorPeriodsController {

    private static final String STATUS_ACTIVE = "Active";
    private static final String STATUS_CONCLUDED = "Concluded";
    private static final String STATUS_UPCOMING = "Upcoming";
    private static final String STATUS_LABEL_ACTIVE = "Activo";
    private static final String STATUS_LABEL_CONCLUDED = "Concluido";
    private static final String STATUS_LABEL_UPCOMING = "Próximo";
    private static final String FORM_TITLE_REGISTER = "Registrar periodo";
    private static final String FORM_TITLE_EDIT = "Editar periodo";
    private static final String NO_VALUE = "—";

    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TextField searchTextField;
    @FXML private TableView<Period> periodsTableView;
    @FXML private TableColumn<Period, String> nameColumn;
    @FXML private TableColumn<Period, String> startColumn;
    @FXML private TableColumn<Period, String> endColumn;
    @FXML private TableColumn<Period, String> statusColumn;

    @FXML private Label formTitleLabel;
    @FXML private FormField nameFormField;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;

    private final PeriodManager periodManager;
    private final ObservableList<Period> allPeriods = FXCollections.observableArrayList();

    private FilteredList<Period> filteredPeriods;
    private Period periodBeingEdited;

    @Inject
    public CoordinatorPeriodsController(PeriodManager periodManager) {
        this.periodManager = periodManager;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadPeriods();
        showListPane();
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

        periodBeingEdited = selectedPeriod;
        prepareFormForEdit(selectedPeriod);
        showFormPane();
    }

    @FXML
    private void handleShowRegisterFormAction() {
        periodBeingEdited = null;
        prepareFormForCreate();
        showFormPane();
    }

    @FXML
    private void handleSaveAction() {
        if (isFormIncomplete()) {
            Controller.showInfoAlert("Campos incompletos", "Por favor, completa el nombre y las fechas del periodo.");
            return;
        }

        if (periodBeingEdited != null) {
            saveEditedPeriod();
        } else {
            saveNewPeriod();
        }
    }

    @FXML
    private void handleCancelFormAction() {
        periodBeingEdited = null;
        showListPane();
    }

    private void saveNewPeriod() {
        try {
            periodManager.registerNewPeriod(buildPeriodFromForm());
            Controller.showSuccessAlert("Registro exitoso", "El periodo académico fue registrado correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error en el registro", e.getMessage());
        }
    }

    private void saveEditedPeriod() {
        try {
            Period period = buildPeriodFromForm();
            periodManager.updatePeriod(period, periodBeingEdited.getPeriodId());
            Controller.showSuccessAlert("Actualización exitosa", "El periodo académico se actualizó correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al actualizar", e.getMessage());
        }
    }

    private void returnToList() {
        periodBeingEdited = null;
        loadPeriods();
        showListPane();
    }

    private Period buildPeriodFromForm() {
        Period period = new Period();
        period.setPeriodName(nameFormField.getText().trim());
        period.setStartDate(Date.valueOf(startDatePicker.getValue()));
        period.setEndDate(Date.valueOf(endDatePicker.getValue()));

        return period;
    }

    private void prepareFormForCreate() {
        formTitleLabel.setText(FORM_TITLE_REGISTER);
        nameFormField.setText("");
        startDatePicker.setValue(null);
        endDatePicker.setValue(null);
    }

    private void prepareFormForEdit(Period period) {
        formTitleLabel.setText(FORM_TITLE_EDIT);
        nameFormField.setText(period.getPeriodName());
        startDatePicker.setValue(toLocalDate(period.getStartDate()));
        endDatePicker.setValue(toLocalDate(period.getEndDate()));
    }

    private boolean isFormIncomplete() {
        return nameFormField.getText().isEmpty()
                || startDatePicker.getValue() == null
                || endDatePicker.getValue() == null;
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

    private LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
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
