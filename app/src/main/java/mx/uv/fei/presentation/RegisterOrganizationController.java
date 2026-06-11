package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.presentation.components.FormField;

import javafx.event.ActionEvent;
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
    private final AppStore store;

    @Inject
    public RegisterOrganizationController(OrganizationManager organizationManager, AppStore store) {
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @FXML
    private void handleActionSaveButton() {
        try {
            Organization organization = buildOrganizationFromForm();
            organizationManager.registerOrganization(organization);
            Controller.showSuccessAlert("Registro Exitoso", "La organización ha sido guardada correctamente.");
            store.dispatch(new NavigationAction.GoToSection(AppSection.ORGANIZATION_MANAGEMENT_MENU));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación", e.getMessage());
        }
    }

    private Organization buildOrganizationFromForm() {
        Organization organization = new Organization();
        organization.setNameOrganization(fieldName.getText());
        organization.setAdress(fieldAddress.getText());
        organization.setCity(fieldCity.getText());
        organization.setBusiness(fieldBusiness.getText());
        organization.setMail(fieldMail.getText());
        organization.setCellphone(fieldCellphone.getText());
        organization.setState(STATUS_ACTIVE);
        return organization;
    }

    @FXML
    private void handleActionCancelButton() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.ORGANIZATION_MANAGEMENT_MENU));
    }
}