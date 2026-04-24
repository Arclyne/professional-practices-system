package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import java.net.URL;
import java.util.ResourceBundle;

import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.RegisterProfessorManager;
import mx.uv.fei.domain.exceptions.ManagerExeption;

public class RegisterProfessorController implements Initializable {

    @FXML private FormField fieldNombre;
    @FXML private FormField fieldApellido;
    @FXML private FormField fieldCorreo;
    @FXML private FormComboBox comboSexo;

    private RegisterProfessorManager manager;

    public void setManager(RegisterProfessorManager manager) {
        this.manager = manager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                "Masculino", "Femenino", "Otro"
        );
        comboSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        if (manager == null) {
            showAlert("Error del Sistema", "Dependencia Manager no inyectada.", AlertType.ERROR);
            return;
        }

        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() || comboSexo.getValue() == null) {
            showAlert("Campos incompletos", "Por favor, llene todos los campos obligatorios del docente.", AlertType.WARNING);
            return;
        }

        Professor newProfessor = new Professor();
        newProfessor.setName(fieldNombre.getText());
        newProfessor.setLastName(fieldApellido.getText());
        newProfessor.setGender(comboSexo.getValue());

        try {
            String generatedPassword = manager.registerNewProfessor(newProfessor);

            showAlert("Registro de Profesor Exitoso",
                    "El docente fue registrado correctamente en la facultad.\nContraseña temporal generada: " + generatedPassword,
                    AlertType.INFORMATION);

            clearForm();

        } catch (ManagerExeption e) {
            showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String message, AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearForm() {
        if (fieldNombre != null) fieldNombre.setText("");
        if (fieldApellido != null) fieldApellido.setText("");
        if (fieldCorreo != null) fieldCorreo.setText("");
        if (comboSexo != null) comboSexo.clearSelection();
    }
}