package mx.uv.fei.presentation;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerLogbookController implements Initializable {

    private static final String TITLE_SUCCESS = "Operación Exitosa";
    private static final String TITLE_ERROR = "Error en la Bitácora";
    private static final String TITLE_VALIDATION = "Campos Incompletos";
    private static final String MSG_VALIDATION = "Por favor, completa los campos obligatorios: Título, Fecha y Descripción.";
    private static final String MSG_DURATION_ERROR = "La duración debe ser un número entero (ej. 2, 4, 8).";

    private final ActivityManager activityManager;
    private final AppStore store;

    private int editingActivityId = -1;

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
        activitiesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);
                if (empty || activity == null) {
                    setText(null);
                } else {
                    String status = activity.getReportId() != null ? "Empaquetada" : "Libre";
                    setText("• " + activity.getTitle() + "\n  Fecha: " + activity.getActivityDate() + " | Horas: " + activity.getDurationHours() + " | Estado: " + status);
                }
            }
        });

        activitiesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null && newSelection.getReportId() == null) {
                btnEditSelected.setDisable(false);
            } else {
                btnEditSelected.setDisable(true);
            }
        });
    }

    private void loadActivitiesLog() {
        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentPractitioner != null ? currentPractitioner.getId() : 0;

            List<Activity> activities = activityManager.getPractitionerLogbook(practitionerId);
            ObservableList<Activity> observableActivities = FXCollections.observableArrayList(activities);
            activitiesListView.setItems(observableActivities);

        } catch (ManagerException e) {
            Controller.showAlert(TITLE_ERROR, e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveActivity(ActionEvent event) {
        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();

            Activity currentActivity = new Activity();
            currentActivity.setPractitionerId(currentPractitioner.getId());
            currentActivity.setTitle(fieldTitle.getText().trim());
            currentActivity.setDescription(textAreaDescription.getText().trim());

            if (datePickerActivity.getValue() != null) {
                currentActivity.setActivityDate(Date.valueOf(datePickerActivity.getValue()));
            }

            int hours = 0;
            if (!fieldDuration.getText().trim().isEmpty()) {
                hours = Integer.parseInt(fieldDuration.getText().trim());
            }
            currentActivity.setDurationHours(hours);

            if (editingActivityId == -1) {
                activityManager.registerActivity(currentActivity);
                Controller.showAlert("Éxito", "Actividad guardada.", AlertType.INFORMATION);
            } else {
                activityManager.modifyActivity(currentActivity, editingActivityId);
                Controller.showAlert("Éxito", "Actividad actualizada.", AlertType.INFORMATION);

                editingActivityId = -1;
                btnSaveActivity.setText("Guardar en Bitácora");
                btnSaveActivity.setStyle("");
            }

            clearForm();
            loadActivitiesLog();

        } catch (NumberFormatException e) {
            Controller.showAlert("Formato Inválido", "Las horas deben ser un número.", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Datos Inválidos", e.getMessage(), AlertType.WARNING);
        }
    }

    @FXML
    private void handleEditSelectedAction(ActionEvent event) {
        Activity selected = activitiesListView.getSelectionModel().getSelectedItem();
        if (selected != null && selected.getReportId() == null) {
            editingActivityId = selected.getActivityId();
            fieldTitle.setText(selected.getTitle());
            datePickerActivity.setValue(selected.getActivityDate().toLocalDate());
            fieldDuration.setText(String.valueOf(selected.getDurationHours()));
            textAreaDescription.setText(selected.getDescription());

            btnSaveActivity.setText("Actualizar Actividad");
            btnSaveActivity.setStyle("-fx-background-color: #F59E0B; -fx-text-fill: white;");
        }
    }

    private void clearForm() {
        fieldTitle.clear();
        textAreaDescription.clear();
        datePickerActivity.setValue(null);
        fieldDuration.clear();

        editingActivityId = -1;
        btnSaveActivity.setText("Guardar en Bitácora");
        btnSaveActivity.setStyle("");
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
