package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RegisterProfessorController implements Initializable {

    @FXML private FormField fieldName;
    @FXML private FormField fieldLastName;
    @FXML private FormField fieldEmail;
    @FXML private FormField fieldPersonalNumber;
    @FXML private FormComboBox comboBoxGender;

    private final ProfessorManager professorManager;
    private final AppStore store;

    @Inject
    public RegisterProfessorController(ProfessorManager professorManager, AppStore store) {
        this.professorManager = professorManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        comboBoxGender.setItems(genderOptions);
    }

    @FXML
    private void handleActionRegisterButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        try {
            Professor professor = buildProfessorFromForm();
            String generatedPassword = professorManager.registerNewProfessor(professor);
            Controller.showAlert("Registro de Profesor Exitoso",
                    "El docente fue registrado correctamente en la facultad.\n"
                            + "Contraseña temporal generada: " + generatedPassword,
                    AlertType.INFORMATION);
            clearForm();
        } catch (ManagerException e) {
            Controller.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private boolean isFormIncomplete() {
        return fieldName.getText().isEmpty()
                || fieldLastName.getText().isEmpty()
                || fieldEmail.getText().isEmpty()
                || fieldPersonalNumber.getText().isEmpty()
                || comboBoxGender.getValue() == null;
    }

    private Professor buildProfessorFromForm() {
        Professor professor = new Professor();
        professor.setName(fieldName.getText().trim());
        professor.setLastName(fieldLastName.getText().trim());
        professor.setEmail(fieldEmail.getText().trim());
        professor.setUserName(fieldPersonalNumber.getText().trim());
        professor.setGender(Gender.fromDisplayValue((String) comboBoxGender.getValue()));
        professor.setRole("Professor");
        professor.setStatus(UserStatus.PENDING);
        return professor;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    private void clearForm() {
        fieldName.setText("");
        fieldLastName.setText("");
        fieldEmail.setText("");
        fieldPersonalNumber.setText("");
        comboBoxGender.clearSelection();
    }
}