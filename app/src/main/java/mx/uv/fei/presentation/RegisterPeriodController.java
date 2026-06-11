package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormField;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.sql.Date;
import java.util.ResourceBundle;

@Component
public class RegisterPeriodController implements Initializable {

    @FXML private FormField fieldPeriodName;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final PeriodManager periodManager;
    private final AppStore store;

    @Inject
    public RegisterPeriodController(PeriodManager periodManager, AppStore store) {
        this.periodManager = periodManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    @FXML
    private void handleActionSaveButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos Incompletos",
                    "Por favor, llene el nombre y seleccione las fechas del periodo.", AlertType.WARNING);
            return;
        }

        try {
            Period period = buildPeriodFromForm();
            periodManager.registerNewPeriod(period);
            Controller.showAlert("Registro Exitoso",
                    "El periodo académico se ha guardado exitosamente.", AlertType.INFORMATION);
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
        } catch (ManagerException e) {
            Controller.showAlert("Fallo en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean isFormIncomplete() {
        return fieldPeriodName.getText().trim().isEmpty()
                || startDatePicker.getValue() == null
                || endDatePicker.getValue() == null;
    }

    private Period buildPeriodFromForm() {
        Period period = new Period();
        period.setPeriodName(fieldPeriodName.getText().trim());
        period.setStartDate(Date.valueOf(startDatePicker.getValue()));
        period.setEndDate(Date.valueOf(endDatePicker.getValue()));
        return period;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
