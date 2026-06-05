package mx.uv.fei.presentation;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerReportGeneratorController implements Initializable {

    private static final String TITLE_SUCCESS = "Reporte Creado";
    private static final String MSG_SUCCESS = "El reporte mensual ha sido generado y las actividades fueron empaquetadas exitosamente.";
    private static final String TITLE_ERROR = "Error al Generar Reporte";
    private static final String TITLE_VALIDATION = "Datos Incompletos";
    private static final String MSG_VALIDATION = "Por favor, completa el mes, el año y el rango de fechas.";
    private static final String MSG_NO_ACTIVITIES = "Debes seleccionar al menos una actividad para generar el reporte.";
    private static final String MSG_DATE_ERROR = "La fecha de inicio no puede ser mayor a la fecha de fin.";
    private static final String MSG_YEAR_ERROR = "El año debe ser un número válido (ej. 2026).";

    private final MonthlyReportManager reportManager;
    private final ActivityManager activityManager;
    private final AppStore store;

    private final List<CheckBox> activityCheckBoxes = new ArrayList<>();

    @FXML private ComboBox<String> comboMonth;
    @FXML private TextField fieldYear;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;
    @FXML private VBox availableActivitiesContainer;

    @Inject
    public PractitionerReportGeneratorController(MonthlyReportManager reportManager, ActivityManager activityManager, AppStore store) {
        this.reportManager = reportManager;
        this.activityManager = activityManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupMonthComboBox();
        fieldYear.setText(String.valueOf(LocalDate.now().getYear()));
        loadAvailableActivities();
    }

    private void setupMonthComboBox() {
        comboMonth.setItems(FXCollections.observableArrayList(
                "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
                "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
        ));
    }

    private void loadAvailableActivities() {
        availableActivitiesContainer.getChildren().clear();
        activityCheckBoxes.clear();

        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentPractitioner != null ? currentPractitioner.getId() : 0;

            List<Activity> allActivities = activityManager.getPractitionerLogbook(practitionerId);

            List<Activity> freeActivities = allActivities.stream()
                    .filter(activity -> activity.getReportId() == null)
                    .toList();

            if (freeActivities.isEmpty()) {
                Label emptyLabel = new Label("No tienes actividades libres. Ve a tu bitácora para registrar nuevas actividades.");
                emptyLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
                emptyLabel.setWrapText(true);
                availableActivitiesContainer.getChildren().add(emptyLabel);
            } else {
                for (Activity activity : freeActivities) {
                    availableActivitiesContainer.getChildren().add(createActivityCheckboxCard(activity));
                }
            }
        } catch (ManagerException exception) {
            Controller.showAlert(TITLE_ERROR, exception.getMessage(), AlertType.ERROR);
        }
    }

    private HBox createActivityCheckboxCard(Activity activity) {
        HBox card = new HBox(15);
        card.setStyle("-fx-background-color: white; -fx-border-color: #E5E7EB; -fx-border-radius: 5px; -fx-padding: 10px;");
        card.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();
        checkBox.setUserData(activity.getActivityId());
        activityCheckBoxes.add(checkBox);

        VBox info = new VBox(3);
        Label title = new Label(activity.getTitle());
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1f2937;");
        Label date = new Label(activity.getActivityDate().toString() + " | " + activity.getDurationHours() + " hrs");
        date.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px;");
        info.getChildren().addAll(title, date);

        card.getChildren().addAll(checkBox, info);

        card.setOnMouseClicked(event -> {
            if (!event.getTarget().equals(checkBox)) {
                checkBox.setSelected(!checkBox.isSelected());
            }
        });

        return card;
    }

    @FXML
    private void handleCreateReportAction(ActionEvent event) {
        if (!validateForm()) return;

        List<Integer> selectedActivityIds = getSelectedActivityIds();

        if (selectedActivityIds.isEmpty()) {
            Controller.showAlert(TITLE_VALIDATION, MSG_NO_ACTIVITIES, AlertType.WARNING);
            return;
        }

        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();

            MonthlyReport newReport = new MonthlyReport();
            newReport.setPractitionerId(currentPractitioner.getId());
            newReport.setMonthName(comboMonth.getValue());
            newReport.setYear(Integer.parseInt(fieldYear.getText().trim()));
            newReport.setStartDate(Date.valueOf(dateStart.getValue()));
            newReport.setEndDate(Date.valueOf(dateEnd.getValue()));
            newReport.setStatus("Pendiente de Firma");

            reportManager.createReportAndLinkActivities(newReport, selectedActivityIds);

            Controller.showAlert(TITLE_SUCCESS, MSG_SUCCESS, AlertType.INFORMATION);

            clearForm();
            loadAvailableActivities();

        } catch (NumberFormatException exception) {
            Controller.showAlert(TITLE_VALIDATION, MSG_YEAR_ERROR, AlertType.WARNING);
        } catch (ManagerException exception) {
            Controller.showAlert(TITLE_ERROR, exception.getMessage(), AlertType.ERROR);
        }
    }

    private boolean validateForm() {
        if (comboMonth.getValue() == null || fieldYear.getText().trim().isEmpty() ||
                dateStart.getValue() == null || dateEnd.getValue() == null) {
            Controller.showAlert(TITLE_VALIDATION, MSG_VALIDATION, AlertType.WARNING);
            return false;
        }

        if (dateStart.getValue().isAfter(dateEnd.getValue())) {
            Controller.showAlert(TITLE_VALIDATION, MSG_DATE_ERROR, AlertType.WARNING);
            return false;
        }

        return true;
    }

    private List<Integer> getSelectedActivityIds() {
        List<Integer> selectedIds = new ArrayList<>();
        for (CheckBox cb : activityCheckBoxes) {
            if (cb.isSelected() && cb.getUserData() != null) {
                selectedIds.add((Integer) cb.getUserData());
            }
        }
        return selectedIds;
    }

    private void clearForm() {
        comboMonth.setValue(null);
        dateStart.setValue(null);
        dateEnd.setValue(null);
    }

    @FXML
    private void handleReturnAction(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}