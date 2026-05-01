package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import java.net.URL;
import java.util.ResourceBundle;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.CommonControler;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.RegisterProfessorManager;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class RegisterProfessorController implements Initializable {

    @FXML
    private FormField fieldNombre;
    @FXML
    private FormField fieldApellido;
    @FXML
    private FormField fieldCorreo;
    @FXML
    private FormField fieldNoPersonal;
    @FXML
    private FormComboBox comboBoxSexo;

    private final RegisterProfessorManager manager;

    @Inject
    public RegisterProfessorController(RegisterProfessorManager manager) {
        this.manager = manager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                "Masculino", "Femenino", "Otro");
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent event) {
        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() || comboBoxSexo.getValue() == null) {
            CommonControler.showAlert("Campos incompletos",
                    "Por favor, llene todos los campos obligatorios del docente.",
                    AlertType.WARNING);
            return;
        }

        Professor newProfessor = new Professor();
        newProfessor.setName(fieldNombre.getText());
        newProfessor.setLastName(fieldApellido.getText());
        newProfessor.setEmail(fieldCorreo.getText());
        newProfessor.setUserName(fieldNoPersonal.getText());
        newProfessor.setGender((String) comboBoxSexo.getValue());

        try {
            String generatedPassword = manager.registerNewProfessor(newProfessor);

            CommonControler.showAlert("Registro de Profesor Exitoso",
                    "El docente fue registrado correctamente en la facultad.\nContraseña temporal generada: "
                            + generatedPassword,
                    AlertType.INFORMATION);

            clearForm();
        } catch (ManagerException e) {
            CommonControler.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }

    private void clearForm() {
        if (fieldNombre != null)
            fieldNombre.setText("");
        if (fieldApellido != null)
            fieldApellido.setText("");
        if (fieldCorreo != null)
            fieldCorreo.setText("");
        if (fieldNoPersonal != null)
            fieldNoPersonal.setText("");
        if (comboBoxSexo != null)
            comboBoxSexo.clearSelection();
    }
}