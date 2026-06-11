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

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class OrganizationManagementController {

    private static final String STATUS_INACTIVE = "Inactive";
    private static final String ORGANIZATION_DISPLAY_FORMAT = "%s (%s) - %s";

    @FXML private ListView<String> organizationsListView;

    private final OrganizationManager organizationManager;
    private final AppStore store;

    private final Map<String, Integer> displayToOrganizationId = new HashMap<>();
    private final Map<String, BooleanProperty> displaySelectionState = new HashMap<>();

    @Inject
    public OrganizationManagementController(OrganizationManager organizationManager, AppStore store) {
        this.organizationManager = organizationManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(organizationsListView, displaySelectionState);
        loadActiveOrganizations();
    }

    private void loadActiveOrganizations() {
        displayToOrganizationId.clear();
        displaySelectionState.clear();
        ObservableList<String> displayItems = FXCollections.observableArrayList();

        try {
            List<Organization> organizations = organizationManager.getAllOrganizations();
            for (Organization organization : organizations) {
                if (isOrganizationActive(organization)) {
                    addOrganizationToDisplay(organization, displayItems);
                }
            }
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }

        organizationsListView.setItems(displayItems);
    }

    private boolean isOrganizationActive(Organization organization) {
        return organization.getState() != null && !STATUS_INACTIVE.equalsIgnoreCase(organization.getState());
    }

    private void addOrganizationToDisplay(Organization organization, ObservableList<String> displayItems) {
        String displayText = String.format(ORGANIZATION_DISPLAY_FORMAT,
                organization.getNameOrganization(), organization.getCity(), organization.getState());
        displayToOrganizationId.put(displayText, organization.getIdOrganization());
        displaySelectionState.put(displayText, new SimpleBooleanProperty(false));
        displayItems.add(displayText);
    }

    @FXML
    private void handleInactivateSelectedAction() {
        List<Integer> selectedOrganizationIds = collectSelectedOrganizationIds();

        if (selectedOrganizationIds.isEmpty()) {
            Controller.showAlert("Sin selección",
                    "Debe seleccionar al menos una organización para inactivar.", AlertType.WARNING);
        } else {
            try {
                organizationManager.inactivateMultipleOrganizations(selectedOrganizationIds);
                Controller.showInfoAlert("Proceso Exitoso",
                        "Las organizaciones seleccionadas han sido inactivadas.");
                loadActiveOrganizations();
            } catch (ManagerException e) {
                Controller.showErrorAlert("Error en la Operación", e.getMessage());
            }
        }
    }

    private List<Integer> collectSelectedOrganizationIds() {
        List<Integer> selectedOrganizationIds = new ArrayList<>();
        for (Map.Entry<String, BooleanProperty> selectionEntry : displaySelectionState.entrySet()) {
            if (selectionEntry.getValue().get()) {
                selectedOrganizationIds.add(displayToOrganizationId.get(selectionEntry.getKey()));
            }
        }
        return selectedOrganizationIds;
    }

    @FXML
    private void handleRegisterNewOrganizationAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_ORGANIZATION));
    }

    @FXML
    private void handleReturnAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}