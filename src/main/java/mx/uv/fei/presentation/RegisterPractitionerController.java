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

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.RegisterPractitionerManager;
import mx.uv.fei.domain.exceptions.ManagerException;

public class RegisterPractitionerController implements Initializable {

    @FXML private FormField fieldMatricula;
    @FXML private FormField fieldNombre;
    @FXML private FormField fieldApellido;
    @FXML private FormField fieldCorreo;

    @FXML private FormComboBox comboBoxSexo;
    @FXML private FormField fieldLengua;

    private RegisterPractitionerManager manager;

    public void setManager(RegisterPractitionerManager manager) {
        this.manager = manager;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                "Masculino", "Femenino", "Otro"
        );
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        Practitioner newPractitioner = new Practitioner();
        newPractitioner.setEnrollment(fieldMatricula.getText());
        newPractitioner.setName(fieldNombre.getText());
        newPractitioner.setLastName(fieldApellido.getText());
        newPractitioner.setEmail(fieldCorreo.getText());
        newPractitioner.setGender(comboBoxSexo.getValue());

        String lengua = fieldLengua.getText().isEmpty() ? "Ninguna" : fieldLengua.getText();
        newPractitioner.setIndigenousLanguage(lengua);

        try {
            String generatedPassword = manager.registerNewPractitioner(newPractitioner);

            showAlert("Registro Exitoso",
                    "El practicante fue registrado correctamente.\nContraseña temporal generada: " + generatedPassword,
                    AlertType.INFORMATION);

            clearForm();

        } catch (ManagerException e) {
            showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        System.out.println("--- Operación Cancelada ---");
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
        if (fieldMatricula != null) fieldMatricula.setText("");
        if (fieldNombre != null) fieldNombre.setText("");
        if (fieldApellido != null) fieldApellido.setText("");
        if (fieldCorreo != null) fieldCorreo.setText("");
        if (fieldLengua != null) fieldLengua.setText("");
    }
}