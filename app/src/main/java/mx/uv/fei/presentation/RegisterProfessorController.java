package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.ResourceBundle;

@Component
public class RegisterProfessorController implements Initializable {

    @FXML private FormField fieldName;
    @FXML private FormField fieldLastName;
    @FXML private FormField fieldEmail;
    @FXML private FormField fieldPersonalNumber;
    @FXML private FormComboBox comboBoxGender;
    @FXML private Button registerButton;

    private final ProfessorManager professorManager;
    private final ShellNavigator shellNavigator;

    private Professor professorBeingEdited;

    @Inject
    public RegisterProfessorController(ProfessorManager professorManager, ShellNavigator shellNavigator) {
        this.professorManager = professorManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> genderOptions = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue());
        comboBoxGender.setItems(genderOptions);

        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Professor) {
            professorBeingEdited = (Professor) pendingEntity;
            populateFormForEdit(professorBeingEdited);
        } else {
            professorBeingEdited = null;
            registerButton.setText("Registrar");
        }
    }

    private void populateFormForEdit(Professor professor) {
        registerButton.setText("Guardar cambios");
        fieldName.setText(professor.getName() != null ? professor.getName() : "");
        fieldLastName.setText(professor.getLastName() != null ? professor.getLastName() : "");
        fieldEmail.setText(professor.getEmail() != null ? professor.getEmail() : "");
        fieldPersonalNumber.setText(professor.getUserName() != null ? professor.getUserName() : "");
        fieldPersonalNumber.setDisable(true);

        if (professor.getGender() != null) {
            comboBoxGender.setValue(professor.getGender().getDisplayValue());
        }
    }

    @FXML
    private void handleActionRegisterButton() {
        if (isFormIncomplete()) {
            Controller.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        if (professorBeingEdited != null) {
            updateProfessor();
        } else {
            registerProfessor();
        }
    }

    private void registerProfessor() {
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

    private void updateProfessor() {
        try {
            applyEditableFields(professorBeingEdited);
            professorManager.updateProfessor(professorBeingEdited, professorBeingEdited.getId());
            Controller.showAlert("Actualización Exitosa",
                    "La información del profesor se actualizó correctamente.", AlertType.INFORMATION);
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showAlert("Error al Actualizar", e.getMessage(), AlertType.ERROR);
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
        applyEditableFields(professor);
        professor.setUserName(fieldPersonalNumber.getText().trim());
        professor.setRole("Professor");
        professor.setStatus(UserStatus.PENDING);
        return professor;
    }

    private void applyEditableFields(Professor professor) {
        professor.setName(fieldName.getText().trim());
        professor.setLastName(fieldLastName.getText().trim());
        professor.setEmail(fieldEmail.getText().trim());
        professor.setGender(Gender.fromDisplayValue((String) comboBoxGender.getValue()));
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        shellNavigator.returnToList();
    }

    private void clearForm() {
        fieldName.setText("");
        fieldLastName.setText("");
        fieldEmail.setText("");
        fieldPersonalNumber.setText("");
        comboBoxGender.clearSelection();
    }
}