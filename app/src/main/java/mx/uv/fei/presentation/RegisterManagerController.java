package mx.uv.fei.presentation;

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

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Component
public class RegisterManagerController implements Initializable {

    private static final String STATUS_ACTIVE = "Active";
    private static final int NO_ORGANIZATION_SELECTED = 0;

    @FXML private FormField fieldName;
    @FXML private FormField fieldPhone;
    @FXML private FormField fieldEmail;
    @FXML private FormComboBox comboBoxOrganization;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final AppStore store;

    private final Map<String, Integer> organizationNameToId = new HashMap<>();

    @Inject
    public RegisterManagerController(OrganizationManager organizationManager,
                                     ManagerManager managerManager, AppStore store) {
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

            for (Organization organization : organizations) {
                if (STATUS_ACTIVE.equalsIgnoreCase(organization.getState())) {
                    organizationOptions.add(organization.getNameOrganization());
                    organizationNameToId.put(organization.getNameOrganization(), organization.getIdOrganization());
                }
            }
            comboBoxOrganization.setItems(organizationOptions);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de Carga", e.getMessage());
        }
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            Manager manager = buildManagerFromForm();
            managerManager.registerManager(manager);
            Controller.showSuccessAlert("Registro Exitoso", "El encargado ha sido guardado correctamente.");
            store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación", e.getMessage());
        }
    }

    private Manager buildManagerFromForm() {
        Manager manager = new Manager();
        manager.setName(fieldName.getText());
        manager.setPhone(fieldPhone.getText());
        manager.setEmail(fieldEmail.getText());
        manager.setStatus(UserStatus.ACTIVE);
        manager.setOrganizationId(resolveSelectedOrganizationId());
        return manager;
    }

    private int resolveSelectedOrganizationId() {
        String selectedOrganization = comboBoxOrganization.getValue();
        if (selectedOrganization != null && organizationNameToId.containsKey(selectedOrganization)) {
            return organizationNameToId.get(selectedOrganization);
        }
        return NO_ORGANIZATION_SELECTED;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}