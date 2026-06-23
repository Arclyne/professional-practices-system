package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.PracticeGroup;
import mx.uv.fei.domain.dto.Period;
import mx.uv.fei.domain.dto.Professor;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.PeriodManager;
import mx.uv.fei.domain.manager.PracticeGroupManager;
import mx.uv.fei.domain.manager.ProfessorManager;
import mx.uv.fei.presentation.components.FormComboBox;
import mx.uv.fei.presentation.components.FormField;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class CoordinatorGroupsController {

    private static final String FORM_TITLE_REGISTER = "Registrar grupo";
    private static final String FORM_TITLE_EDIT = "Editar grupo";
    private static final String UNKNOWN_VALUE = "—";

    @FXML private VBox listPane;
    @FXML private VBox formPane;

    @FXML private TextField searchTextField;
    @FXML private TableView<PracticeGroup> groupsTableView;
    @FXML private TableColumn<PracticeGroup, String> sectionColumn;
    @FXML private TableColumn<PracticeGroup, String> periodColumn;
    @FXML private TableColumn<PracticeGroup, String> professorColumn;

    @FXML private Label formTitleLabel;
    @FXML private FormField sectionFormField;
    @FXML private FormComboBox periodFormComboBox;
    @FXML private FormComboBox professorFormComboBox;

    private final PracticeGroupManager practiceGroupManager;
    private final PeriodManager periodManager;
    private final ProfessorManager professorManager;
    private final ObservableList<PracticeGroup> allGroups = FXCollections.observableArrayList();

    private FilteredList<PracticeGroup> filteredGroups;
    private PracticeGroup groupBeingEdited;
    private Map<Integer, String> periodNamesById = new HashMap<>();
    private Map<String, Integer> periodIdsByName = new HashMap<>();
    private Map<Integer, String> professorNamesById = new HashMap<>();
    private Map<String, Integer> professorIdsByName = new HashMap<>();

    @Inject
    public CoordinatorGroupsController(PracticeGroupManager practiceGroupManager, PeriodManager periodManager,
            ProfessorManager professorManager) {
        this.practiceGroupManager = practiceGroupManager;
        this.periodManager = periodManager;
        this.professorManager = professorManager;
    }

    @FXML
    public void initialize() {
        setupColumns();
        bindFilteredTable();
        loadGroups();
        showListPane();
    }

    private void setupColumns() {
        sectionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSection()));
        periodColumn.setCellValueFactory(data -> new SimpleStringProperty(periodNameOf(data.getValue())));
        professorColumn.setCellValueFactory(data -> new SimpleStringProperty(professorNameOf(data.getValue())));
    }

    private void bindFilteredTable() {
        filteredGroups = new FilteredList<>(allGroups);
        groupsTableView.setItems(filteredGroups);
    }

    private void loadGroups() {
        try {
            mapPeriods(periodManager.getAllPeriods());
            mapProfessors(professorManager.getAllProfessors());
            periodFormComboBox.setItems(FXCollections.observableArrayList(periodIdsByName.keySet()));
            professorFormComboBox.setItems(FXCollections.observableArrayList(professorIdsByName.keySet()));
            allGroups.setAll(practiceGroupManager.getAllPracticeGroups());
            applyFilter();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error de carga", e.getMessage());
        }
    }

    private void mapPeriods(List<Period> periods) {
        periodNamesById = new HashMap<>();
        periodIdsByName = new HashMap<>();
        for (Period period : periods) {
            periodNamesById.put(period.getPeriodId(), period.getPeriodName());
            periodIdsByName.put(period.getPeriodName(), period.getPeriodId());
        }
    }

    private void mapProfessors(List<Professor> professors) {
        professorNamesById = new HashMap<>();
        professorIdsByName = new HashMap<>();
        for (Professor professor : professors) {
            String fullName = professor.getName() + " " + professor.getLastName();
            professorNamesById.put(professor.getId(), fullName);
            professorIdsByName.put(fullName, professor.getId());
        }
    }

    @FXML
    private void handleSearchAction() {
        applyFilter();
    }

    @FXML
    private void handleRefreshAction() {
        loadGroups();
    }

    @FXML
    private void handleEditAction() {
        PracticeGroup selectedGroup = groupsTableView.getSelectionModel().getSelectedItem();
        if (selectedGroup == null) {
            Controller.showInfoAlert("Selección requerida", "Selecciona un grupo de la lista para editarlo.");
            return;
        }

        groupBeingEdited = selectedGroup;
        prepareFormForEdit(selectedGroup);
        showFormPane();
    }

    @FXML
    private void handleShowRegisterFormAction() {
        groupBeingEdited = null;
        prepareFormForCreate();
        showFormPane();
    }

    @FXML
    private void handleSaveAction() {
        if (isFormIncomplete()) {
            Controller.showInfoAlert("Campos incompletos", "Por favor, completa la sección, el periodo y el profesor.");
            return;
        }

        if (groupBeingEdited != null) {
            saveEditedGroup();
        } else {
            saveNewGroup();
        }
    }

    @FXML
    private void handleCancelFormAction() {
        groupBeingEdited = null;
        showListPane();
    }

    private void saveNewGroup() {
        try {
            practiceGroupManager.registerNewPracticeGroup(buildGroupFromForm());
            Controller.showSuccessAlert("Registro exitoso", "El grupo de prácticas fue registrado correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error en el registro", e.getMessage());
        }
    }

    private void saveEditedGroup() {
        try {
            practiceGroupManager.updatePracticeGroup(buildGroupFromForm(), groupBeingEdited.getGroupId());
            Controller.showSuccessAlert("Actualización exitosa", "El grupo de prácticas se actualizó correctamente.");
            returnToList();
        } catch (ManagerException e) {
            Controller.showErrorAlert("Error al actualizar", e.getMessage());
        }
    }

    private void returnToList() {
        groupBeingEdited = null;
        loadGroups();
        showListPane();
    }

    private PracticeGroup buildGroupFromForm() {
        PracticeGroup group = new PracticeGroup();
        group.setSection(sectionFormField.getText().trim());
        group.setPeriodId(periodIdsByName.getOrDefault(periodFormComboBox.getValue(), 0));
        group.setProfessorId(professorIdsByName.getOrDefault(professorFormComboBox.getValue(), 0));

        return group;
    }

    private void prepareFormForCreate() {
        formTitleLabel.setText(FORM_TITLE_REGISTER);
        sectionFormField.setText("");
        periodFormComboBox.clearSelection();
        professorFormComboBox.clearSelection();
    }

    private void prepareFormForEdit(PracticeGroup group) {
        formTitleLabel.setText(FORM_TITLE_EDIT);
        sectionFormField.setText(group.getSection());
        periodFormComboBox.valueProperty().set(periodNameOf(group));
        professorFormComboBox.valueProperty().set(professorNameOf(group));
    }

    private boolean isFormIncomplete() {
        return sectionFormField.getText().isEmpty()
                || periodFormComboBox.getValue() == null
                || professorFormComboBox.getValue() == null;
    }

    private void applyFilter() {
        filteredGroups.setPredicate(this::matchesSearchText);
    }

    private boolean matchesSearchText(PracticeGroup group) {
        String query = searchTextField.getText() == null ? "" : searchTextField.getText().trim().toLowerCase();
        String searchableText = (group.getSection() + " " + periodNameOf(group) + " "
                + professorNameOf(group)).toLowerCase();

        return query.isEmpty() || searchableText.contains(query);
    }

    private String periodNameOf(PracticeGroup group) {
        return periodNamesById.getOrDefault(group.getPeriodId(), UNKNOWN_VALUE);
    }

    private String professorNameOf(PracticeGroup group) {
        return professorNamesById.getOrDefault(group.getProfessorId(), UNKNOWN_VALUE);
    }

    private void showListPane() {
        setNodeVisible(listPane, true);
        setNodeVisible(formPane, false);
    }

    private void showFormPane() {
        setNodeVisible(listPane, false);
        setNodeVisible(formPane, true);
    }

    private void setNodeVisible(Node node, boolean isVisible) {
        node.setVisible(isVisible);
        node.setManaged(isVisible);
    }
}
