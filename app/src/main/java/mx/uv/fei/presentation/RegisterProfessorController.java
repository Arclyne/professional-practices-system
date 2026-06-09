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
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.enums.Gender;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.domain.exceptions.ManagerException;

@Component
public class RegisterProfessorController implements Initializable {

    private final ProfessorManager manager;
    private final AppStore store;

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

    @Inject
    public RegisterProfessorController(ProfessorManager manager, AppStore store) {
        this.manager = manager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ObservableList<String> opcionesSexo = FXCollections.observableArrayList(
                Gender.MALE.getDisplayValue(),
                Gender.FEMALE.getDisplayValue(),
                Gender.OTHER.getDisplayValue()
        );
        comboBoxSexo.setItems(opcionesSexo);
    }

    @FXML
    private void handleActionRegisterButton(ActionEvent event) {
        if (fieldNombre.getText().isEmpty() || fieldApellido.getText().isEmpty() ||
                fieldCorreo.getText().isEmpty() || fieldNoPersonal.getText().isEmpty() ||
                comboBoxSexo.getValue() == null) {

            Controller.showAlert("Campos incompletos", "Por favor, llene todos los campos obligatorios.", AlertType.WARNING);
            return;
        }

        Professor newProfessor = createProfessor();

        try {
            String generatedPassword = manager.registerNewProfessor(newProfessor);

            Controller.showAlert("Registro de Profesor Exitoso",
                    "El docente fue registrado correctamente en la facultad.\nContraseña temporal generada: "
                            + generatedPassword,
                    AlertType.INFORMATION);

            clearForm();
        } catch (ManagerException e) {
            Controller.showAlert("Error en el Registro", e.getMessage(), AlertType.ERROR);
        }
    }

    private Professor createProfessor() {
        Professor newProfessor = new Professor();
        newProfessor.setName(fieldNombre.getText().trim());
        newProfessor.setLastName(fieldApellido.getText().trim());
        newProfessor.setEmail(fieldCorreo.getText().trim());
        newProfessor.setUserName(fieldNoPersonal.getText().trim());

        String selectedSex = (String) comboBoxSexo.getValue();
        newProfessor.setGender(Gender.fromDisplayValue(selectedSex));

        newProfessor.setRole("Professor");
        newProfessor.setStatus(UserStatus.PENDING);

        return newProfessor;
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
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
