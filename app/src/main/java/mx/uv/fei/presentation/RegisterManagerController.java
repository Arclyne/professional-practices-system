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
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
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

    private static final int NO_ORGANIZATION_SELECTED = 0;

    @FXML private FormField fieldName;
    @FXML private FormField fieldPhone;
    @FXML private FormField fieldEmail;
    @FXML private FormComboBox comboBoxOrganization;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final OrganizationManager organizationManager;
    private final ManagerManager managerManager;
    private final ShellNavigator shellNavigator;

    private final Map<String, Integer> organizationIdByName = new HashMap<>();
    private final Map<Integer, String> organizationNameById = new HashMap<>();

    private Manager managerBeingEdited;

    @Inject
    public RegisterManagerController(OrganizationManager organizationManager,
                                     ManagerManager managerManager, ShellNavigator shellNavigator) {
        this.organizationManager = organizationManager;
        this.managerManager = managerManager;
        this.shellNavigator = shellNavigator;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadOrganizations();

        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Manager) {
            managerBeingEdited = (Manager) pendingEntity;
            populateFormForEdit(managerBeingEdited);
        } else {
            managerBeingEdited = null;
            saveButton.setText("Registrar");
        }
    }

    private void loadOrganizations() {
        try {
            List<Organization> organizations = organizationManager.getAllOrganizations();
            ObservableList<String> organizationOptions = FXCollections.observableArrayList();

            for (Organization organization : organizations) {
                organizationOptions.add(organization.getNameOrganization());
                organizationIdByName.put(organization.getNameOrganization(), organization.getIdOrganization());
                organizationNameById.put(organization.getIdOrganization(), organization.getNameOrganization());
            }
            comboBoxOrganization.setItems(organizationOptions);
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de Carga", e.getMessage());
        }
    }

    private void populateFormForEdit(Manager manager) {
        saveButton.setText("Guardar cambios");
        fieldName.setText(manager.getName() != null ? manager.getName() : "");
        fieldPhone.setText(manager.getPhone() != null ? manager.getPhone() : "");
        fieldEmail.setText(manager.getEmail() != null ? manager.getEmail() : "");
        comboBoxOrganization.setValue(organizationNameById.get(manager.getOrganizationId()));
    }

    @FXML
    private void handleActionSaveButton() {
        if (isFormIncomplete()) {
            Controller.showInfoAlert("Campos incompletos", "Por favor, completa todos los datos del encargado.");
            return;
        }

        if (managerBeingEdited != null) {
            updateManager();
        } else {
            registerManager();
        }
    }

    private void registerManager() {
        try {
            Manager manager = buildManagerFromForm();
            manager.setStatus(UserStatus.ACTIVE);
            managerManager.registerManager(manager);
            Controller.showSuccessAlert("Registro Exitoso", "El encargado ha sido guardado correctamente.");
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación", e.getMessage());
        }
    }

    private void updateManager() {
        try {
            Manager manager = buildManagerFromForm();
            managerManager.updateManager(manager, managerBeingEdited.getId());
            Controller.showSuccessAlert("Actualización exitosa", "El encargado se actualizó correctamente.");
            shellNavigator.returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al actualizar", e.getMessage());
        }
    }

    private boolean isFormIncomplete() {
        return fieldName.getText().isEmpty()
                || fieldPhone.getText().isEmpty()
                || fieldEmail.getText().isEmpty()
                || comboBoxOrganization.getValue() == null;
    }

    private Manager buildManagerFromForm() {
        Manager manager = new Manager();
        manager.setName(fieldName.getText().trim());
        manager.setPhone(fieldPhone.getText().trim());
        manager.setEmail(fieldEmail.getText().trim());
        manager.setOrganizationId(resolveSelectedOrganizationId());
        return manager;
    }

    private int resolveSelectedOrganizationId() {
        String selectedOrganization = comboBoxOrganization.getValue();
        return organizationIdByName.getOrDefault(selectedOrganization, NO_ORGANIZATION_SELECTED);
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }
}
