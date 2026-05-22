package mx.uv.fei.presentation;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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
import mx.uv.fei.domain.manager.ManagerManager;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;


@Component
public class RegisterManagerController implements Initializable {

    private static final String STATUS_ACTIVE = "Activo";
    private static final String LOAD_ERROR_TITLE = "Error de Carga";
    private static final String SUCCESS_TITLE = "Registro Exitoso";
    private static final String SUCCESS_MESSAGE = "El encargado ha sido guardado correctamente.";
    private static final String VALIDATION_ERROR_TITLE = "Validación";

    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final AppStore store;

    private final Map<String, Integer> organizationMap = new HashMap<>();

    @FXML private FormField fieldName;
    @FXML private FormField fieldPhone;
    @FXML private FormField fieldEmail;
    @FXML private FormComboBox comboBoxOrganization;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Inject
    public RegisterManagerController(OrganizationManager organizationManager,
                                     ManagerManager managerManager,
                                     AppStore store) {
        this.organizationManager = organizationManager;
        this.managerManager = managerManager;
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
                if (STATUS_ACTIVE.equalsIgnoreCase(org.getState())) {
                    organizationOptions.add(org.getNameOrganization());
                    organizationMap.put(org.getNameOrganization(), org.getIdOrganization());
                }
            }
            comboBoxOrganization.setItems(organizationOptions);

        } catch (ManagerException exception) {
            Controller.showErrorAlert(LOAD_ERROR_TITLE, exception.getMessage());
        }
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            Manager managerToRegister = new Manager();
            managerToRegister.setName(fieldName.getText());
            managerToRegister.setPhone(fieldPhone.getText());
            managerToRegister.setEmail(fieldEmail.getText());
            managerToRegister.setStatus(UserStatus.ACTIVE);

            String selectedOrg = (String) comboBoxOrganization.getValue();
            int orgId = (selectedOrg != null && organizationMap.containsKey(selectedOrg))
                    ? organizationMap.get(selectedOrg)
                    : 0;

            managerToRegister.setOrganizationId(orgId);

            if (managerManager.registerManager(managerToRegister)) {
                Controller.showInfoAlert(SUCCESS_TITLE, SUCCESS_MESSAGE);
                store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
            }

        } catch (ManagerException exception) {
            Controller.showErrorAlert(VALIDATION_ERROR_TITLE, exception.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}