package mx.uv.fei.presentation;

import java.net.URL;
import java.sql.Date;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

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

    private static final String TITLE_SUCCESS = "Actividad Registrada";
    private static final String MSG_SUCCESS = "Tu actividad se ha guardado en la bitácora.";
    private static final String TITLE_ERROR = "Error en la Bitácora";
    private static final String TITLE_VALIDATION = "Campos Incompletos";
    private static final String MSG_VALIDATION = "Por favor, completa los campos obligatorios: Título, Fecha y Descripción.";
    private static final String MSG_DURATION_ERROR = "La duración debe ser un número entero (ej. 2, 4, 8).";

    private final ActivityManager activityManager;
    private final AppStore store;

    @FXML private TextField fieldTitle;
    @FXML private DatePicker datePickerActivity;
    @FXML private TextField fieldDuration;
    @FXML private TextArea textAreaDescription;
    @FXML private VBox activitiesContainer;

    @Inject
    public PractitionerLogbookController(ActivityManager activityManager, AppStore store) {
        this.activityManager = activityManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadActivitiesLog();
    }

    private void loadActivitiesLog() {
        activitiesContainer.getChildren().clear();

        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentPractitioner != null ? currentPractitioner.getId() : 0;

            List<Activity> activities = activityManager.getPractitionerLogbook(practitionerId);

            if (activities.isEmpty()) {
                Label emptyLabel = new Label("Tu bitácora está vacía. Registra tu primera actividad.");
                emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 14px; -fx-font-style: italic;");
                activitiesContainer.getChildren().add(emptyLabel);
            } else {
                for (Activity activity : activities) {
                    activitiesContainer.getChildren().add(createActivityCard(activity));
                }
            }
        } catch (ManagerException exception) {
            Controller.showAlert(TITLE_ERROR, exception.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleSaveActivity(ActionEvent event) {
        if (!validateForm()) return;

        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();

            Activity newActivity = new Activity();
            newActivity.setPractitionerId(currentPractitioner.getId());
            newActivity.setTitle(fieldTitle.getText().trim());
            newActivity.setDescription(textAreaDescription.getText().trim());
            newActivity.setActivityDate(Date.valueOf(datePickerActivity.getValue()));

            if (!fieldDuration.getText().trim().isEmpty()) {
                newActivity.setDurationHours(Integer.parseInt(fieldDuration.getText().trim()));
            } else {
                newActivity.setDurationHours(0);
            }

            activityManager.registerActivity(newActivity);

            Controller.showAlert(TITLE_SUCCESS, MSG_SUCCESS, AlertType.INFORMATION);
            clearForm();
            loadActivitiesLog();

        } catch (NumberFormatException e) {
            Controller.showAlert(TITLE_VALIDATION, MSG_DURATION_ERROR, AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert(TITLE_ERROR, e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        if (fieldTitle.getText().trim().isEmpty() ||
                textAreaDescription.getText().trim().isEmpty() ||
                datePickerActivity.getValue() == null) {
            Controller.showAlert(TITLE_VALIDATION, MSG_VALIDATION, AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void clearForm() {
        fieldTitle.clear();
        textAreaDescription.clear();
        datePickerActivity.setValue(null);
        fieldDuration.clear();
    }

    private VBox createActivityCard(Activity activity) {
        VBox card = new VBox(8);
        String borderColor = activity.getReportId() != null ? "#3B82F6" : "#10B981";
        card.setStyle("-fx-background-color: white; -fx-border-color: " + borderColor + "; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 15px;");

        Label titleLabel = new Label(activity.getTitle());
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #1f2937;");

        Label infoLabel = new Label("Fecha: " + activity.getActivityDate() + " | Duración: " + activity.getDurationHours() + " hrs");
        infoLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");

        Label descLabel = new Label(activity.getDescription());
        descLabel.setWrapText(true);
        descLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: #4b5563;");

        HBox bottomRow = new HBox(15);
        bottomRow.setPadding(new Insets(10, 0, 0, 0));

        Label statusLabel = new Label(activity.getReportId() != null ? "Empaquetada en Reporte" : "Actividad Libre");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: " + borderColor + ";");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        bottomRow.getChildren().addAll(statusLabel, spacer);

        card.getChildren().addAll(titleLabel, infoLabel, descLabel, bottomRow);
        return card;
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}