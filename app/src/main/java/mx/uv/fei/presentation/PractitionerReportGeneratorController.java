package mx.uv.fei.presentation;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Activity;
import mx.uv.fei.domain.dto.MonthlyReport;
import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.enums.ReportStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ActivityManager;
import mx.uv.fei.domain.manager.MonthlyReportManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class PractitionerReportGeneratorController implements Initializable {

    private static final String TITLE_SUCCESS = "Reporte Creado";
    private static final String TITLE_ERROR = "Error al Generar Reporte";
    private static final String TITLE_VALIDATION = "Datos Incompletos";
    private static final String MSG_VALIDATION = "Por favor, completa el mes, el año y el rango de fechas.";

    private final MonthlyReportManager reportManager;
    private final ActivityManager activityManager;
    private final AppStore store;

    @FXML private ComboBox<String> comboMonth;
    @FXML private TextField fieldYear;
    @FXML private DatePicker dateStart;
    @FXML private DatePicker dateEnd;

    @FXML private ListView<Activity> freeActivitiesListView;
    @FXML private Button btnQuickEdit;

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
        configureListView();
        loadAvailableActivities();
    }

    private void setupMonthComboBox() {
        Locale spanishLocale = Locale.of("es", "MX");

        List<String> translatedMonths = Arrays.stream(Month.values())
                .map(month -> month.getDisplayName(TextStyle.FULL, spanishLocale))
                .map(name -> name.substring(0, 1).toUpperCase() + name.substring(1))
                .toList();

        comboMonth.setItems(FXCollections.observableArrayList(translatedMonths));
    }

    private void configureListView() {
        freeActivitiesListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        freeActivitiesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(Activity activity, boolean empty) {
                super.updateItem(activity, empty);
                if (empty || activity == null) {
                    setText(null);
                } else {
                    setText("• " + activity.getTitle() + " | Fecha: " + activity.getActivityDate() + " | " + activity.getDurationHours() + " hrs");
                }
            }
        });

        freeActivitiesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            btnQuickEdit.setDisable(newSelection == null);
        });
    }

    private void loadAvailableActivities() {
        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();
            int practitionerId = currentPractitioner != null ? currentPractitioner.getId() : 0;

            List<Activity> allActivities = activityManager.getPractitionerLogbook(practitionerId);

            List<Activity> freeActivities = allActivities.stream()
                    .filter(activity -> activity.getReportId() == null)
                    .toList();

            ObservableList<Activity> observableActivities = FXCollections.observableArrayList(freeActivities);
            freeActivitiesListView.setItems(observableActivities);

        } catch (ManagerException e) {
            Controller.showAlert(TITLE_ERROR, e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleQuickEditAction(ActionEvent event) {
        Activity selected = freeActivitiesListView.getSelectionModel().getSelectedItem();
        if (selected != null) {
            openQuickEditDialog(selected);
        }
    }

    private void openQuickEditDialog(Activity activity) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Editar Actividad Libre");
        dialog.setHeaderText("Modifica los datos de la actividad.");
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10); grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField titleField = new TextField(activity.getTitle());
        DatePicker datePicker = new DatePicker(activity.getActivityDate().toLocalDate());
        TextField hoursField = new TextField(String.valueOf(activity.getDurationHours()));
        TextArea descArea = new TextArea(activity.getDescription());
        descArea.setPrefRowCount(3);

        grid.add(new Label("Título:"), 0, 0); grid.add(titleField, 1, 0);
        grid.add(new Label("Fecha:"), 0, 1);  grid.add(datePicker, 1, 1);
        grid.add(new Label("Horas:"), 0, 2);  grid.add(hoursField, 1, 2);
        grid.add(new Label("Descripción:"), 0, 3); grid.add(descArea, 1, 3);

        dialog.getDialogPane().setContent(grid);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                activity.setTitle(titleField.getText().trim());
                activity.setActivityDate(Date.valueOf(datePicker.getValue()));
                activity.setDurationHours(Integer.parseInt(hoursField.getText().trim()));
                activity.setDescription(descArea.getText().trim());

                activityManager.modifyActivity(activity, activity.getActivityId());
                Controller.showAlert("Actualización Exitosa", "Actividad modificada correctamente.", AlertType.INFORMATION);

                loadAvailableActivities();

            } catch (NumberFormatException e) {
                Controller.showAlert("Error de Formato", "Las horas deben ser un número.", AlertType.WARNING);
            } catch (ManagerException e) {
                Controller.showAlert("Error", e.getMessage(), AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleCreateReportAction(ActionEvent event) {
        try {
            User currentPractitioner = store.getState().sessionState().currentUserInSession();

            MonthlyReport newReport = new MonthlyReport();
            newReport.setPractitionerId(currentPractitioner.getId());
            newReport.setMonthName(comboMonth.getValue());

            int year = 0;
            if (!fieldYear.getText().trim().isEmpty()) {
                year = Integer.parseInt(fieldYear.getText().trim());
            }
            newReport.setYear(year);

            if (dateStart.getValue() != null) {
                newReport.setStartDate(Date.valueOf(dateStart.getValue()));
            }
            if (dateEnd.getValue() != null) {
                newReport.setEndDate(Date.valueOf(dateEnd.getValue()));
            }

            newReport.setStatus(ReportStatus.PENDING.getDatabaseValue());

            List<Activity> selectedActivities = getSelectedActivities();
            reportManager.createReportAndLinkActivities(newReport, selectedActivities);

            Controller.showAlert("Reporte Creado", "El reporte se generó con éxito.", AlertType.INFORMATION);

            clearForm();
            loadAvailableActivities();

        } catch (NumberFormatException e) {
            Controller.showAlert("Formato Inválido", "El año debe ser un número.", AlertType.WARNING);
        } catch (ManagerException e) {
            Controller.showAlert("Datos Inválidos", e.getMessage(), AlertType.WARNING);
        }
    }

    private List<Activity> getSelectedActivities() {
        return new ArrayList<>(freeActivitiesListView.getSelectionModel().getSelectedItems());
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