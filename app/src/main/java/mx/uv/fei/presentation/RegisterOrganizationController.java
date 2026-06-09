package mx.uv.fei.presentation;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

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

@Component
public class RegisterOrganizationController{

    private final OrganizationManager organizationManager;
    private final AppStore store;

    @FXML private FormField fieldName;
    @FXML private FormField fieldAddress;
    @FXML private FormField fieldCity;
    @FXML private FormField fieldBusiness;
    @FXML private FormField fieldMail;
    @FXML private FormField fieldCellphone;

    @FXML private Button saveButton;
    @FXML private Button cancelButton;

    @Inject
    public RegisterOrganizationController(OrganizationManager organizationManager, AppStore store) {
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @FXML
    private void handleActionSaveButton(ActionEvent event) {
        try {
            Organization organizationToRegister = new Organization();
            organizationToRegister.setNameOrganization(fieldName.getText());
            organizationToRegister.setAdress(fieldAddress.getText());
            organizationToRegister.setCity(fieldCity.getText());
            organizationToRegister.setBusiness(fieldBusiness.getText());
            organizationToRegister.setMail(fieldMail.getText());
            organizationToRegister.setCellphone(fieldCellphone.getText());
            organizationToRegister.setState("Active");

            organizationManager.registerOrganization(organizationToRegister);

            Controller.showSuccessAlert("Registro Exitoso", "La organización ha sido guardada correctamente.");
            store.dispatch(new NavigationAction.GoToSection(AppSection.ORGANIZATION_MANAGEMENT_MENU));
        } catch (ManagerException e) {
            Controller.showErrorAlert("Validación", e.getMessage());
        }
    }

    @FXML
    private void handleActionCancelButton(ActionEvent event) {
        store.dispatch(new NavigationAction.GoToSection(AppSection.ORGANIZATION_MANAGEMENT_MENU));
    }
}