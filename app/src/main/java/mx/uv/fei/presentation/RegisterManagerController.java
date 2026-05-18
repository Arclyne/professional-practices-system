package mx.uv.fei.presentation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Manager;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.enums.UserStatus;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.Store;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class RegisterManagerController implements Initializable {

    private final OrganizationManager organizationManager;
    private final Store store;

    private final Map<String, Integer> organizationMap = new HashMap<>();

    @FXML private FormField fieldName;
    @FXML private FormField fieldPhone;
    @FXML private FormField fieldEmail;
    @FXML private FormComboBox comboBoxOrganization;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Inject
    public RegisterManagerController(OrganizationManager organizationManager, Store store) {
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadOrganizations();
    }

    private void loadOrganizations() {
        try {
            List<Organization> organizations = organizationManager.getAllOrganizations();
            ObservableList<String> organizationOptions = FXCollections.observableArrayList();

            for (Organization org : organizations) {
                if ("Activo".equalsIgnoreCase(org.getState())) {
                    organizationOptions.add(org.getNameOrganization());
                    organizationMap.put(org.getNameOrganization(), org.getIdOrganization());
                }
            }
            comboBoxOrganization.setItems(organizationOptions);

        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de Carga", e.getMessage());
        }
    }

    @FXML
    private void handleActionSaveButton(ActionEvent event) {
        try {
            Manager managerToRegister = new Manager();
            managerToRegister.setName(fieldName.getText());
            managerToRegister.setPhone(fieldPhone.getText());
            managerToRegister.setEmail(fieldEmail.getText());
            managerToRegister.setStatus(UserStatus.ACTIVE);


            String selectedOrg = comboBoxOrganization.getValue();

            int orgId = (selectedOrg != null && organizationMap.containsKey(selectedOrg))
                    ? organizationMap.get(selectedOrg)
                    : 0;

            managerToRegister.setOrganizationId(orgId);

            boolean isSaved = organizationManager.registerManager(managerToRegister);

            if (isSaved) {
                Controller.showSuccessAlert("Registro Exitoso", "El encargado ha sido guardado correctamente.");
                store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
            }

        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación", e.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}