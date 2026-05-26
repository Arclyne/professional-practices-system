package mx.uv.fei.presentation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;


import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Organization;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.OrganizationManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;


@Component
public class OrganizationManagementController {

    private static final String STATUS_INACTIVE = "Inactive";
    private static final String LOAD_ERROR_TITLE = "Error de carga";
    private static final String NO_SELECTION_TITLE = "Sin selección";
    private static final String NO_SELECTION_MESSAGE = "Debe seleccionar al menos una organización para inactivar.";
    private static final String SUCCESS_TITLE = "Proceso Exitoso";
    private static final String SUCCESS_MESSAGE = "Las organizaciones seleccionadas han sido inactivadas.";
    private static final String OPERATION_ERROR_TITLE = "Error en la Operación";
    private static final String DISPLAY_ITEM_FORMAT = "%s (%s) - %s";

    @FXML
    private ListView<String> organizationsListView;

    private final OrganizationManager organizationManager;
    private final AppStore applicationNavigationStore;

    private final Map<String, Integer> itemToIdentifierMap = new HashMap<>();
    private final Map<String, BooleanProperty> itemSelectionStateMap = new HashMap<>();

    @Inject
    public OrganizationManagementController(OrganizationManager organizationManager, AppStore applicationNavigationStore) {
        this.organizationManager = organizationManager;
        this.applicationNavigationStore = applicationNavigationStore;
    }

    @FXML
    public void initialize() {
        Controller.setupCheckBoxListView(organizationsListView, itemSelectionStateMap);
        loadActiveOrganizations();
    }

    private void loadActiveOrganizations() {
        itemToIdentifierMap.clear();
        itemSelectionStateMap.clear();
        ObservableList<String> displayItemsList = FXCollections.observableArrayList();

        try {
            List<Organization> registeredOrganizationsList = organizationManager.getAllOrganizations();

            for (Organization currentOrganization : registeredOrganizationsList) {
                if (currentOrganization.getState() != null && !STATUS_INACTIVE.equalsIgnoreCase(currentOrganization.getState())) {
                    String formattedDisplayString = String.format(DISPLAY_ITEM_FORMAT, currentOrganization.getNameOrganization(), currentOrganization.getCity(), currentOrganization.getState());
                    itemToIdentifierMap.put(formattedDisplayString, currentOrganization.getIdOrganization());
                    itemSelectionStateMap.put(formattedDisplayString, new SimpleBooleanProperty(false));
                    displayItemsList.add(formattedDisplayString);
                }
            }
        } catch (ManagerException exception) {
            Controller.showErrorAlert(LOAD_ERROR_TITLE, exception.getMessage());
        }

        organizationsListView.setItems(displayItemsList);
    }

    @FXML
    private void handleInactivateSelectedAction() {
        List<Integer> identifiersToInactivateList = new ArrayList<>();

        for (Map.Entry<String, BooleanProperty> currentMapEntry : itemSelectionStateMap.entrySet()) {
            if (currentMapEntry.getValue().get()) {
                identifiersToInactivateList.add(itemToIdentifierMap.get(currentMapEntry.getKey()));
            }
        }

        if (identifiersToInactivateList.isEmpty()) {
            Controller.showAlert(NO_SELECTION_TITLE, NO_SELECTION_MESSAGE, AlertType.WARNING);
        } else {
            try {
                organizationManager.inactivateMultipleOrganizations(identifiersToInactivateList);
                Controller.showInfoAlert(SUCCESS_TITLE, SUCCESS_MESSAGE);
                loadActiveOrganizations();
            } catch (ManagerException exception) {
                Controller.showErrorAlert(OPERATION_ERROR_TITLE, exception.getMessage());
            }
        }
    }

    @FXML
    private void handleRegisterNewOrganizationAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_ORGANIZATION));
    }

    @FXML
    private void handleReturnAction() {
        applicationNavigationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}