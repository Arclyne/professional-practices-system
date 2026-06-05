package mx.uv.fei.presentation;

import java.net.URL;
import java.time.format.DateTimeParseException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.common.Parse;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormField;

@Component
public class RegisterPeriodController implements Initializable {

    private static final String TITLE_SUCCESS = "Registro Exitoso";
    private static final String MSG_SUCCESS = "El periodo académico se ha guardado exitosamente.";
    private static final String TITLE_ERROR = "Fallo en el Registro";
    private static final String TITLE_VALIDATION_ERROR = "Campos Incompletos";
    private static final String MSG_EMPTY_FIELDS = "Por favor, llene todos los campos obligatorios.";
    private static final String TITLE_DATE_ERROR = "Error de Fecha";
    private static final String MSG_DATE_ERROR = "Por favor, introduzca fechas válidas en formato numérico (DD/MM/AAAA).";

    private final PeriodManager periodManager;
    private final AppStore applicationNavigationStore;

    @FXML private FormField fieldPeriodName;
    @FXML private TextField textFieldStartDay;
    @FXML private TextField textFieldStartMonth;
    @FXML private TextField textFieldStartYear;
    @FXML private TextField textFieldDeadlineDay;
    @FXML private TextField textFieldDeadlineMonth;
    @FXML private TextField textFieldDeadlineYear;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Inject
    public RegisterPeriodController(PeriodManager periodManager, AppStore applicationNavigationStore) {
        this.periodManager = periodManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @Override
    public void initialize(URL locationUrl, ResourceBundle resourcesBundle) {
    }

    @FXML
    private void handleActionSaveButton(ActionEvent event) {
        if (validateFields()) {
            try {
                Period newPeriod = createPeriod();
                periodManager.registerNewPeriod(newPeriod);

                Controller.showAlert(TITLE_SUCCESS, MSG_SUCCESS, AlertType.INFORMATION);
                applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));

            } catch (IllegalArgumentException | DateTimeParseException dateException) {
                Controller.showAlert(TITLE_DATE_ERROR, MSG_DATE_ERROR, AlertType.WARNING);
            } catch (ManagerException exception) {
                Controller.showAlert(TITLE_ERROR, exception.getMessage(), AlertType.ERROR);
            }
        }
    }

    private boolean validateFields() {
        boolean isValid = true;

        if (fieldPeriodName.getText().trim().isEmpty() ||
                textFieldStartDay.getText().trim().isEmpty() ||
                textFieldStartMonth.getText().trim().isEmpty() ||
                textFieldStartYear.getText().trim().isEmpty() ||
                textFieldDeadlineDay.getText().trim().isEmpty() ||
                textFieldDeadlineMonth.getText().trim().isEmpty() ||
                textFieldDeadlineYear.getText().trim().isEmpty()) {

            Controller.showAlert(TITLE_VALIDATION_ERROR, MSG_EMPTY_FIELDS, AlertType.WARNING);
            isValid = false;
        }

        return isValid;
    }

    private Period createPeriod() {
        Period period = new Period();
        period.setPeriodName(fieldPeriodName.getText().trim());

        period.setStartDate(Parse.parseDate(
                textFieldStartDay.getText().trim(),
                textFieldStartMonth.getText().trim(),
                textFieldStartYear.getText().trim()
        ));

        period.setEndDate(Parse.parseDate(
                textFieldDeadlineDay.getText().trim(),
                textFieldDeadlineMonth.getText().trim(),
                textFieldDeadlineYear.getText().trim()
        ));

        return period;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}