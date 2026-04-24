package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.scene.control.Button;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;

public class RegisterPractitionerController implements Initializable {

    public Button registerButton;
    public Button cancelButton;
    @FXML private FormField fieldStudentId;
    @FXML private FormField fieldName;
    @FXML private FormField fieldEmail;

    @FXML private FormComboBox comboSexo;
    @FXML private FormComboBox comboLengua;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                "Masculino", "Femenino", "Prefiero no decirlo"
        );
        comboSexo.setItems(opcionesSexo);

        ObservableList<String> opcionesLengua = FXCollections.observableArrayList(
                "Ninguna", "Náhuatl", "Totonaco", "Zapoteco", "Otra"
        );
        comboLengua.setItems(opcionesLengua);
    }

    @FXML
    private void handleRegisterButtonAction(ActionEvent event) {
        String matricula = fieldStudentId.getText();
        String nombre = fieldName.getText();
        String correo = fieldEmail.getText();
        String sexo = comboSexo.getValue();
        String lengua = comboLengua.getValue();

        System.out.println("--- Intento de Registro en Sistema FEI ---");
        System.out.println("Matrícula: " + matricula);
        System.out.println("Nombre: " + nombre);
        System.out.println("Correo: " + correo);
        System.out.println("Sexo: " + sexo);
        System.out.println("Lengua Indígena: " + lengua);
        System.out.println("------------------------------------------");
    }

    @FXML
    private void handleCancelButtonAction(ActionEvent event) {
        System.out.println("--- Operación Cancelada por el Usuario ---");

        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Stage stage = (javafx.stage.Stage) source.getScene().getWindow();
        stage.close();
    }
}