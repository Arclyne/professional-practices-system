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
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.common.Controller;

@Component
public class RegisterPractitionerController implements Initializable {

    @FXML
    private FormField fieldMatricula;
    @FXML
    private FormField fieldNombre;
    @FXML
    private FormField fieldApellido;
    @FXML
    private FormField fieldCorreo;
    @FXML
    private FormComboBox comboBoxSexo;
    @FXML
    private FormField fieldLengua;

    private final PractitionerManager manager;
    private final Store store;

    @Inject
    public RegisterPractitionerController(PractitionerManager manager, Store store) {
        this.manager = manager;
        this.store = store;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                "Masculino", "Femenino", "Otro");
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent event) {
        Practitioner newPractitioner = createPractitioner();

        String lengua = fieldLengua.getText().isEmpty() ? "Ninguna" : fieldLengua.getText();
        newPractitioner.setIndigenousLanguage(lengua);

        try {
            String generatedPassword = manager.registerNewPractitioner(newPractitioner);

            Controller.showAlert("Registro Exitoso",
                    "El practicante fue registrado correctamente.\nContraseña temporal generada: " + generatedPassword,
                    AlertType.INFORMATION);

            clearForm();

        } catch (ManagerException e) {
            Controller.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private Practitioner createPractitioner() {
        Practitioner newPractitioner = new Practitioner();
        newPractitioner.setEnrollment(fieldMatricula.getText());
        newPractitioner.setName(fieldNombre.getText());
        newPractitioner.setLastName(fieldApellido.getText());
        newPractitioner.setEmail(fieldCorreo.getText());
        newPractitioner.setGender((String) comboBoxSexo.getValue());

        return newPractitioner;
    }


    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }

    private void clearForm() {
        if (fieldMatricula != null)
            fieldMatricula.setText("");
        if (fieldNombre != null)
            fieldNombre.setText("");
        if (fieldApellido != null)
            fieldApellido.setText("");
        if (fieldCorreo != null)
            fieldCorreo.setText("");
        if (fieldLengua != null)
            fieldLengua.setText("");
    }
}