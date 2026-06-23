package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.ProfessorManager;
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
public class CoordinatorProfessorsController {

    private static final String NO_STATUS_LABEL = "—";
    private static final int DOUBLE_CLICK_COUNT = 2;

    @FXML private TextField searchTextField;
    @FXML private TableView<Professor> professorsTableView;
    @FXML private TableColumn<Professor, String> nameColumn;
    @FXML private TableColumn<Professor, String> personalNumberColumn;
    @FXML private TableColumn<Professor, String> emailColumn;
    @FXML private TableColumn<Professor, String> statusColumn;

    private final ProfessorManager professorManager;
    private final AppStore store;
    private final ObservableList<Professor> allProfessors = FXCollections.observableArrayList();

    private FilteredList<Professor> filteredProfessors;

    @Inject
    public CoordinatorProfessorsController(ProfessorManager professorManager, AppStore store) {
        this.professorManager = professorManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        setupTableInteraction();
        loadProfessors();
    }

    private void setupColumns() {
        nameColumn.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getName() + " " + data.getValue().getLastName()));
        personalNumberColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getUserName()));
        emailColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        statusColumn.setCellValueFactory(data -> new SimpleStringProperty(statusLabelOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredProfessors = new FilteredList<>(allProfessors);
        professorsTableView.setItems(filteredProfessors);
    }

    private void loadProfessors() {
        try {
            allProfessors.setAll(professorManager.getAllProfessors());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void setupTableInteraction() {
        professorsTableView.setOnMouseClicked(this::handleProfessorRowClick);
    }

    private void handleProfessorRowClick(MouseEvent event) {
        Professor selectedProfessor = professorsTableView.getSelectionModel().getSelectedItem();
        if (event.getClickCount() == DOUBLE_CLICK_COUNT && selectedProfessor != null) {
            store.dispatch(new NavigationAction.ViewEntityDetails(
                    AppSection.REGISTER_PROFESSOR, String.valueOf(selectedProfessor.getId())));
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadProfessors();
    }

    @FXML
    private void handleRegisterAction() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.REGISTER_PROFESSOR));
    }

    private void applyFilter() {
        filteredProfessors.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(Professor professor) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (professor.getName() + " " + professor.getLastName() + " "
                + professor.getUserName() + " " + professor.getEmail()).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String statusLabelOf(Professor professor) {
        return professor.getStatus() != null ? professor.getStatus().getDisplayLabel() : NO_STATUS_LABEL;
    }
}
