package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.academic.PeriodManager;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;

import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ResourceBundle;

@Component
public class RegisterPeriodController implements Initializable {

    @FXML private FormField fieldPeriodName;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final PeriodManager periodManager;
    private final ShellNavigator shellNavigator;

    private Period periodBeingEdited;

    @Inject
    public RegisterPeriodController(PeriodManager periodManager, ShellNavigator shellNavigator) {
        this.periodManager = periodManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Period) {
            periodBeingEdited = (Period) pendingEntity;
            populateFormForEdit(periodBeingEdited);
        } else {
            periodBeingEdited = null;
            saveButton.setText("Guardar Periodo");
        }
    }

    private void populateFormForEdit(Period period) {
        saveButton.setText("Guardar cambios");
        fieldPeriodName.setText(period.getPeriodName() != null ? period.getPeriodName() : "");
        startDatePicker.setValue(toLocalDate(period.getStartDate()));
        endDatePicker.setValue(toLocalDate(period.getEndDate()));
    }

    @FXML
    private void handleActionSaveButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos Incompletos",
                    "Por favor, llene el nombre y seleccione las fechas del periodo.", AlertType.WARNING);
        } else if (periodBeingEdited != null) {
            updatePeriod();
        } else {
            registerPeriod();
        }
    }

    private void registerPeriod() {
        try {
            periodManager.registerNewPeriod(buildPeriodFromForm());
            Controller.showAlert("Registro Exitoso",
                    "El periodo académico se ha guardado exitosamente.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Fallo en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private void updatePeriod() {
        try {
            periodManager.updatePeriod(buildPeriodFromForm(), periodBeingEdited.getPeriodId());
            Controller.showAlert("Actualización Exitosa",
                    "El periodo académico se actualizó correctamente.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.ERROR);
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

    private LocalDate toLocalDate(Date date) {
        return date != null ? date.toLocalDate() : null;
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }
}
