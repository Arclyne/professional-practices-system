package mx.uv.fei.presentation.practitioner;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.ActivityManager;
import mx.uv.fei.domain.statemachine.AppStore;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

@Component
public class PractitionerLogbookController implements Initializable {

    private static final int NO_ACTIVITY_IN_EDITION = -1;
    private static final String SAVE_BUTTON_DEFAULT_TEXT = "Guardar en bitácora";
    private static final String SAVE_BUTTON_EDIT_TEXT = "Actualizar actividad";
    private static final String EDIT_BUTTON_STYLE = "-fx-background-color: #F59E0B; -fx-text-fill: white;";

    private final ActivityManager activityManager;
    private final AppStore store;

    private int editingActivityId = NO_ACTIVITY_IN_EDITION;

    @FXML private TextField fieldTitle;
    @FXML private DatePicker datePickerActivity;
    @FXML private TextField fieldDuration;
    @FXML private TextArea textAreaDescription;
    @FXML private Button btnSaveActivity;
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
        loadActivitiesLog();
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
                + "\n  Fecha: " + activity.getActivityDate()
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
            List<Activity> activities = activityManager.getPractitionerLogbook(practitionerId);
            ObservableList<Activity> activityItems = FXCollections.observableArrayList(activities);
            activitiesListView.setItems(activityItems);
        } catch (ManagerException e) {
            Controller.showAlert("Error en la Bitácora", e.getMessage(), AlertType.ERROR);
        }
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

        if (datePickerActivity.getValue() != null) {
            activity.setActivityDate(Date.valueOf(datePickerActivity.getValue()));
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
            datePickerActivity.setValue(selectedActivity.getActivityDate().toLocalDate());
            fieldDuration.setText(String.valueOf(selectedActivity.getDurationHours()));
            textAreaDescription.setText(selectedActivity.getDescription());
            btnSaveActivity.setText(SAVE_BUTTON_EDIT_TEXT);
            btnSaveActivity.setStyle(EDIT_BUTTON_STYLE);
        }
    }

    private void clearForm() {
        fieldTitle.clear();
        textAreaDescription.clear();
        datePickerActivity.setValue(null);
        fieldDuration.clear();
        editingActivityId = NO_ACTIVITY_IN_EDITION;
        btnSaveActivity.setText(SAVE_BUTTON_DEFAULT_TEXT);
        btnSaveActivity.setStyle("");
    }
}