package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PractitionerManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

@Component
public class CoordinatorPractitionersController {

    private static final String NO_GROUP_LABEL = "Sin grupo";
    private static final String GROUP_PREFIX = "Grupo ";
    private static final String NO_STATUS_LABEL = "—";
    private static final int DOUBLE_CLICK_COUNT = 2;

    @FXML private TextField searchTextField;
    @FXML private TableView<Practitioner> practitionersTableView;
    @FXML private TableColumn<Practitioner, String> nameColumn;
    @FXML private TableColumn<Practitioner, String> enrollmentColumn;
    @FXML private TableColumn<Practitioner, String> groupColumn;
    @FXML private TableColumn<Practitioner, String> statusColumn;

    private final PractitionerManager practitionerManager;
    private final AppStore store;
    private final ObservableList<Practitioner> allPractitioners = FXCollections.observableArrayList();

    private FilteredList<Practitioner> filteredPractitioners;

    @Inject
    public CoordinatorPractitionersController(PractitionerManager practitionerManager, AppStore store) {
        this.practitionerManager = practitionerManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        setupTableInteraction();
        loadPractitioners();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getName() + " " + data.getValue().getLastName()));
        enrollmentColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEnrollment()));
        groupColumn.setCellValueFactory(data -> new SimpleStringProperty(groupLabelOf(data.getValue())));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void setupTableInteraction() {
        practitionersTableView.setOnMouseClicked(this::handlePractitionerRowClick);
    }

    private void handlePractitionerRowClick(MouseEvent event) {
        Practitioner selectedPractitioner = practitionersTableView.getSelectionModel().getSelectedItem();
        if (event.getClickCount() == DOUBLE_CLICK_COUNT && selectedPractitioner != null) {
            store.dispatch(new NavigationAction.ViewEntityDetails(
                    AppSection.REGISTER_PRACTITIONER, String.valueOf(selectedPractitioner.getId())));
        }
    }

    private void bindFilteredTable() {
        filteredPractitioners = new FilteredList<>(allPractitioners);
        practitionersTableView.setItems(filteredPractitioners);
    }

    private void loadPractitioners() {
        try {
            allPractitioners.setAll(practitionerManager.getAllPractitioners());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadPractitioners();
    }

    @FXML
    private void handleRegisterAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PRACTITIONER));
    }

    private void applyFilter() {
        filteredPractitioners.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Practitioner practitioner) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (practitioner.getName() + " " + practitioner.getLastName() + " "
                + practitioner.getEnrollment()).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String groupLabelOf(Practitioner practitioner) {
        return practitioner.getGroupId() != null ? GROUP_PREFIX + practitioner.getGroupId() : NO_GROUP_LABEL;
    }

    private String statusLabelOf(Practitioner practitioner) {
        return practitioner.getStatus() != null ? practitioner.getStatus().getDisplayLabel() : NO_STATUS_LABEL;
    }
}
