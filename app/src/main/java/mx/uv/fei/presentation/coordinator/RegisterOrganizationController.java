package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.people.OrganizationManager;
import mx.uv.fei.presentation.components.FormField;
import mx.uv.fei.presentation.shell.ShellNavigator;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

@Component
public class RegisterOrganizationController {

    private static final String STATUS_ACTIVE = "Active";

    @FXML private FormField fieldName;
    @FXML private FormField fieldAddress;
    @FXML private FormField fieldCity;
    @FXML private FormField fieldBusiness;
    @FXML private FormField fieldMail;
    @FXML private FormField fieldCellphone;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    private final OrganizationManager organizationManager;
    private final ShellNavigator shellNavigator;
    private Organization organizationBeingEdited;

    @Inject
    public RegisterOrganizationController(OrganizationManager organizationManager, ShellNavigator shellNavigator) {
        this.organizationManager = organizationManager;
        this.shellNavigator = shellNavigator;
    }

    @FXML
    public void initialize() {
        Object pendingEntity = shellNavigator.consumePendingEntity();
        if (pendingEntity instanceof Organization) {
            organizationBeingEdited = (Organization) pendingEntity;
            populateForm(organizationBeingEdited);
            saveButton.setText("Guardar cambios");
        } else {
            organizationBeingEdited = null;
            saveButton.setText("Guardar");
        }
    }

    private void populateForm(Organization organization) {
        fieldName.setText(organization.getNameOrganization() != null ? organization.getNameOrganization() : "");
        fieldBusiness.setText(organization.getBusiness() != null ? organization.getBusiness() : "");
        fieldAddress.setText(organization.getAdress() != null ? organization.getAdress() : "");
        fieldCity.setText(organization.getCity() != null ? organization.getCity() : "");
        fieldMail.setText(organization.getMail() != null ? organization.getMail() : "");
        fieldCellphone.setText(organization.getCellphone() != null ? organization.getCellphone() : "");
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            Organization organization = buildOrganizationFromForm();

            if (organizationBeingEdited != null) {
                organizationManager.updateOrganization(organization, organizationBeingEdited.getIdOrganization());
                Controller.showSuccessAlert("Actualización Exitosa", "La organización se ha actualizado correctamente.");
            } else {
                organizationManager.registerOrganization(organization);
                Controller.showSuccessAlert("Registro Exitoso", "La organización ha sido guardada correctamente.");
            }
            shellNavigator.returnToList();

        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación", e.getMessage());
        }
    }

    private Organization buildOrganizationFromForm() {
        Organization organization = new Organization();
        organization.setNameOrganization(fieldName.getText().trim());
        organization.setAdress(fieldAddress.getText().trim());
        organization.setCity(fieldCity.getText().trim());
        organization.setBusiness(fieldBusiness.getText().trim());
        organization.setMail(fieldMail.getText().trim());
        organization.setCellphone(fieldCellphone.getText().trim());
        organization.setState(STATUS_ACTIVE);
        return organization;
    }

    @FXML
    private void handleActionCancelButton() {
        shellNavigator.returnToList();
    }
}