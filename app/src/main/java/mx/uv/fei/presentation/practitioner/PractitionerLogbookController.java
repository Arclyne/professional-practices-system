package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.Month;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.ActivityManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Date;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

@Component
public class PractitionerLogbookController implements Initializable {

    private static final int NO_ACTIVITY_IN_EDITION = -1;
    private static final String SAVE_BUTTON_DEFAULT_TEXT = "Guardar en bitácora";
    private static final String SAVE_BUTTON_EDIT_TEXT = "Actualizar actividad";
    private static final String EDIT_BUTTON_STYLE = "-fx-background-color: #F59E0B; -fx-text-fill: white;";
    private static final String STATUS_FILTER_ALL = "Todas";
    private static final String STATUS_FILTER_FREE = "Libres";
    private static final String STATUS_FILTER_PACKAGED = "Empaquetadas";
    private static final String MONTH_FILTER_ALL = "Todos los meses";

    private final ActivityManager activityManager;
    private final AppStore store;
    private final List<Activity> allActivities = new ArrayList<>();

    private int editingActivityId = NO_ACTIVITY_IN_EDITION;

    @FXML private TextField fieldTitle;
    @FXML private DatePicker datePickerStart;
    @FXML private DatePicker datePickerEnd;
    @FXML private TextField fieldDuration;
    @FXML private TextArea textAreaDescription;
    @FXML private Button btnSaveActivity;
    @FXML private ComboBox<String> statusFilterComboBox;
    @FXML private ComboBox<String> monthFilterComboBox;
    @FXML private ListView<Activity> activitiesListView;
    @FXML private Button btnEditSelected;

    @Inject
    public PractitionerLogbookController(ActivityManager activityManager, AppStore store) {
        this.activityManager = activityManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureListView();
        configureFilters();
        loadActivitiesLog();
    }

    private void configureFilters() {
        statusFilterComboBox.setItems(FXCollections.observableArrayList(
                STATUS_FILTER_ALL, STATUS_FILTER_FREE, STATUS_FILTER_PACKAGED));
        statusFilterComboBox.setValue(STATUS_FILTER_ALL);
        statusFilterComboBox.valueProperty().addListener((_, _, _) -> applyFilters());
        monthFilterComboBox.valueProperty().addListener((_, _, _) -> applyFilters());
    }

    private void configureListView() {
        activitiesListView.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Activity activity, boolean isEmpty) {
                super.updateItem(activity, isEmpty);
                if (isEmpty || activity == null) {
                    setText(null);
                } else {
                    setText(buildActivityDisplayText(activity));
                }
            }
        });

        activitiesListView.getSelectionModel().selectedItemProperty()
                .addListener((_, _, selectedActivity) ->
                        btnEditSelected.setDisable(!isActivityEditable(selectedActivity)));
    }

    private String buildActivityDisplayText(Activity activity) {
        String status = activity.getReportId() != null ? "Empaquetada" : "Libre";
        return "• " + activity.getTitle()
                + "\n  Fechas: " + activity.getStartDate() + " al " + activity.getEndDate()
                + " | Horas: " + activity.getDurationHours()
                + " | Estado: " + status;
    }

    private boolean isActivityEditable(Activity activity) {
        return activity != null && activity.getReportId() == null;
    }

    private void loadActivitiesLog() {
        try {
            User currentUser = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentUser != null ? currentUser.getId() : 0;
            allActivities.clear();
            allActivities.addAll(activityManager.getPractitionerLogbook(practitionerId));
            populateMonthFilter();
            applyFilters();
        } catch (ManagerException e) {
            Controller.showAlert("Error en la Bitácora", e.getMessage(), AlertType.ERROR);
        }
    }

    private void populateMonthFilter() {
        Set<String> monthOptions = new LinkedHashSet<>();
        monthOptions.add(MONTH_FILTER_ALL);
        for (Activity activity : allActivities) {
            monthOptions.add(monthLabel(activity));
        }
        String previousSelection = monthFilterComboBox.getValue();
        monthFilterComboBox.setItems(FXCollections.observableArrayList(monthOptions));
        monthFilterComboBox.setValue(monthOptions.contains(previousSelection) ? previousSelection : MONTH_FILTER_ALL);
    }

    private void applyFilters() {
        List<Activity> visibleActivities = new ArrayList<>();
        for (Activity activity : allActivities) {
            if (matchesStatusFilter(activity) && matchesMonthFilter(activity)) {
                visibleActivities.add(activity);
            }
        }
        activitiesListView.setItems(FXCollections.observableArrayList(visibleActivities));
    }

    private boolean matchesStatusFilter(Activity activity) {
        String selectedStatus = statusFilterComboBox.getValue();
        boolean isMatch = selectedStatus == null || STATUS_FILTER_ALL.equals(selectedStatus);

        if (!isMatch && STATUS_FILTER_FREE.equals(selectedStatus)) {
            isMatch = activity.getReportId() == null;
        } else if (!isMatch && STATUS_FILTER_PACKAGED.equals(selectedStatus)) {
            isMatch = activity.getReportId() != null;
        }

        return isMatch;
    }

    private boolean matchesMonthFilter(Activity activity) {
        String selectedMonth = monthFilterComboBox.getValue();
        return selectedMonth == null || MONTH_FILTER_ALL.equals(selectedMonth) || selectedMonth.equals(monthLabel(activity));
    }

    private String monthLabel(Activity activity) {
        java.time.LocalDate startDate = activity.getStartDate().toLocalDate();
        return monthName(startDate.getMonthValue()) + " " + startDate.getYear();
    }

    private String monthName(int monthNumber) {
        String monthName = String.valueOf(monthNumber);
        for (Month month : Month.values()) {
            if (month.getMonthNumber() == monthNumber) {
                monthName = month.name().charAt(0) + month.name().substring(1).toLowerCase();
            }
        }
        return monthName;
    }

    @FXML
    private void handleSaveActivity() {
        try {
            Activity activity = buildActivityFromForm();
            if (editingActivityId == NO_ACTIVITY_IN_EDITION) {
                activityManager.registerActivity(activity);
                Controller.showAlert("Éxito", "Actividad guardada.", AlertType.INFORMATION);
            } else {
                activityManager.modifyActivity(activity, editingActivityId);
                Controller.showAlert("Éxito", "Actividad actualizada.", AlertType.INFORMATION);
            }
            clearForm();
            loadActivitiesLog();
        } catch (NumberFormatException e) {
            Controller.showAlert("Formato Inválido", "Las horas deben ser un número.", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Datos Inválidos", e.getMessage(), AlertType.WARNING);
        }
    }

    private Activity buildActivityFromForm() {
        User currentUser = store.getState().sessionState().currentUserInSession();
        Activity activity = new Activity();
        activity.setPractitionerId(currentUser.getId());
        activity.setTitle(fieldTitle.getText().trim());
        activity.setDescription(textAreaDescription.getText().trim());

        if (datePickerStart.getValue() != null) {
            activity.setStartDate(Date.valueOf(datePickerStart.getValue()));
        }
        if (datePickerEnd.getValue() != null) {
            activity.setEndDate(Date.valueOf(datePickerEnd.getValue()));
        }
        activity.setDurationHours(parseDurationHours());
        return activity;
    }

    private int parseDurationHours() {
        String durationText = fieldDuration.getText().trim();
        return durationText.isEmpty() ? 0 : Integer.parseInt(durationText);
    }

    @FXML
    private void handleEditSelectedAction() {
        Activity selectedActivity = activitiesListView.getSelectionModel().getSelectedItem();
        if (isActivityEditable(selectedActivity)) {
            editingActivityId = selectedActivity.getActivityId();
            fieldTitle.setText(selectedActivity.getTitle());
            datePickerStart.setValue(selectedActivity.getStartDate().toLocalDate());
            datePickerEnd.setValue(selectedActivity.getEndDate().toLocalDate());
            fieldDuration.setText(String.valueOf(selectedActivity.getDurationHours()));
            textAreaDescription.setText(selectedActivity.getDescription());
            btnSaveActivity.setText(SAVE_BUTTON_EDIT_TEXT);
            btnSaveActivity.setStyle(EDIT_BUTTON_STYLE);
        }
    }

    private void clearForm() {
        fieldTitle.clear();
        textAreaDescription.clear();
        datePickerStart.setValue(null);
        datePickerEnd.setValue(null);
        fieldDuration.clear();
        editingActivityId = NO_ACTIVITY_IN_EDITION;
        btnSaveActivity.setText(SAVE_BUTTON_DEFAULT_TEXT);
        btnSaveActivity.setStyle("");
    }
}