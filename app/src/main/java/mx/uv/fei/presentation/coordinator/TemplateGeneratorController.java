package mx.uv.fei.presentation.coordinator;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.DocumentTemplate;
import mx.uv.fei.domain.enums.TemplateToken;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.reporting.TemplateManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.dto.User;


import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class TemplateGeneratorController {

    private static final String TYPE_LOGBOOK = "Bitácora de Reporte Mensual";
    private static final String TYPE_OFFICIAL_DOCUMENT = "Oficio Oficial";
    private static final String TOKEN_CURRENT_DATE = "%FECHA_ACTUAL%";
    private static final String DATE_PATTERN = "dd/MM/yyyy";
    private static final String BATCH_COUNT_PREFIX = "Documentos en cola: ";
    private static final String DEFAULT_CREATOR_USERNAME = "coordinadora";
    private static final String OUTPUT_DIRECTORY_HINT = "app/documents/generated/";

    @FXML private TextField templateNameField;
    @FXML private ComboBox<String> documentTypeComboBox;
    @FXML private ListView<TemplateToken> availableTokensListView;
    @FXML private TextArea templateBodyTextArea;
    @FXML private Button saveTemplateButton;
    @FXML private Button insertTokenButton;
    @FXML private Button clearEditorButton;
    @FXML private Button backToDashboardButton;
    @FXML private ListView<DocumentTemplate> savedTemplatesListView;
    @FXML private VBox tokenValuesContainer;
    @FXML private TextArea documentPreviewTextArea;
    @FXML private Button loadTemplateButton;
    @FXML private Button previewButton;
    @FXML private Button generateSingleButton;
    @FXML private Button addToBatchButton;
    @FXML private Label batchCountLabel;
    @FXML private Button generateBatchButton;
    @FXML private Button deleteTemplateButton;

    private final TemplateManager templateManager;
    private final AppStore store;

    private final List<Map<String, String>> batchQueue = new ArrayList<>();
    private final Map<String, TextField> tokenInputFields = new HashMap<>();

    private DocumentTemplate currentlyEditingTemplate;

    @Inject
    public TemplateGeneratorController(TemplateManager templateManager, AppStore store) {
        this.templateManager = templateManager;
        this.store = store;
    }

    @FXML
    public void initialize() {
        setupEditorTab();
        setupUsageTab();
        refreshSavedTemplatesList();
    }

    private void setupEditorTab() {
        documentTypeComboBox.setItems(FXCollections.observableArrayList(TYPE_LOGBOOK, TYPE_OFFICIAL_DOCUMENT));
        documentTypeComboBox.getSelectionModel().selectFirst();

        availableTokensListView.setItems(FXCollections.observableArrayList(TemplateToken.values()));
        availableTokensListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                insertSelectedToken();
            }
        });
    }

    private void setupUsageTab() {
        batchCountLabel.setText(BATCH_COUNT_PREFIX + 0);
        tokenValuesContainer.getChildren().clear();
    }

    @FXML
    private void handleInsertToken() {
        insertSelectedToken();
    }

    private void insertSelectedToken() {
        TemplateToken selectedToken = availableTokensListView.getSelectionModel().getSelectedItem();
        if (selectedToken != null) {
            int caretPosition = templateBodyTextArea.getCaretPosition();
            templateBodyTextArea.insertText(caretPosition, selectedToken.getPlaceholder());
            templateBodyTextArea.requestFocus();
        }
    }

    @FXML
    private void handleClearEditor() {
        clearEditor();
    }

    private void clearEditor() {
        templateNameField.clear();
        templateBodyTextArea.clear();
        documentTypeComboBox.getSelectionModel().selectFirst();
        currentlyEditingTemplate = null;
    }

    @FXML
    private void handleSaveTemplate() {
        String templateName = templateNameField.getText();
        String documentType = documentTypeComboBox.getValue();
        String bodyContent = templateBodyTextArea.getText();

        if (isBlank(templateName)) {
            Controller.showAlert("Campo requerido",
                    "El nombre de la plantilla no puede estar vacío.", AlertType.WARNING);
        } else if (isBlank(bodyContent)) {
            Controller.showAlert("Campo requerido",
                    "El cuerpo de la plantilla no puede estar vacío.", AlertType.WARNING);
        } else {
            persistTemplate(templateName.trim(), documentType, bodyContent);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void persistTemplate(String templateName, String documentType, String bodyContent) {
        DocumentTemplate templateToSave = buildTemplateFromEditor(templateName, documentType, bodyContent);
        try {
            templateManager.saveTemplate(templateToSave);
            Controller.showAlert("Plantilla guardada",
                    "La plantilla \"" + templateName + "\" se guardó correctamente.", AlertType.INFORMATION);
            clearEditor();
            refreshSavedTemplatesList();
        } catch (ManagerException e) {
            Controller.showAlert("Error al guardar",
                    "No se pudo guardar la plantilla. " + e.getMessage(), AlertType.ERROR);
        }
    }

    private DocumentTemplate buildTemplateFromEditor(String name, String type, String body) {
        DocumentTemplate template = new DocumentTemplate();
        if (currentlyEditingTemplate != null) {
            template.setTemplateId(currentlyEditingTemplate.getTemplateId());
        }
        template.setTemplateName(name);
        template.setDocumentType(type);
        template.setBodyContent(body);
        template.setCreatedByUserName(resolveCurrentUserName());
        template.setCreatedAt(LocalDate.now());
        return template;
    }

    private String resolveCurrentUserName() {
        User currentUser = currentUserOrNull();
        return currentUser != null ? currentUser.getUserName() : DEFAULT_CREATOR_USERNAME;
    }

    private User currentUserOrNull() {
        if (store.getState() == null || store.getState().sessionState() == null) {
            return null;
        }
        return store.getState().sessionState().currentUserInSession();
    }

    private void refreshSavedTemplatesList() {
        try {
            List<DocumentTemplate> allTemplates = templateManager.loadAllTemplates();
            savedTemplatesListView.setItems(FXCollections.observableArrayList(allTemplates));
        } catch (ManagerException e) {
            Controller.showAlert("Error al cargar",
                    "No se pudieron cargar las plantillas. " + e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleLoadTemplate() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Selecciona una plantilla de la lista antes de cargarla.");
        } else {
            buildTokenInputFields(selectedTemplate);
            documentPreviewTextArea.clear();
            resetBatchQueue();
        }
    }

    private void buildTokenInputFields(DocumentTemplate template) {
        tokenValuesContainer.getChildren().clear();
        tokenInputFields.clear();

        String body = template.getBodyContent();
        for (TemplateToken token : TemplateToken.values()) {
            if (body.contains(token.getPlaceholder())) {
                addTokenInputRow(token);
            }
        }

        if (tokenValuesContainer.getChildren().isEmpty()) {
            addNoTokensLabel();
        }
    }

    private void addNoTokensLabel() {
        Label noTokensLabel = new Label("Esta plantilla no contiene tokens reemplazables.");
        noTokensLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
        tokenValuesContainer.getChildren().add(noTokensLabel);
    }

    private void addTokenInputRow(TemplateToken token) {
        Label tokenLabel = new Label(token.getDescription() + ":");
        tokenLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        TextField tokenInput = new TextField();
        tokenInput.setPromptText("Valor para " + token.getPlaceholder());
        tokenInput.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8px; -fx-padding: 6px;");
        HBox.setHgrow(tokenInput, Priority.ALWAYS);

        if (TOKEN_CURRENT_DATE.equals(token.getPlaceholder())) {
            tokenInput.setText(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        }

        tokenInputFields.put(token.getPlaceholder(), tokenInput);

        VBox rowContainer = new VBox(4, tokenLabel, tokenInput);
        rowContainer.setPadding(new Insets(0, 0, 8, 0));
        tokenValuesContainer.getChildren().add(rowContainer);
    }

    @FXML
    private void handlePreviewDocument() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Carga una plantilla primero.");
        } else {
            String renderedContent = templateManager.renderTemplate(selectedTemplate, collectCurrentTokenValues());
            documentPreviewTextArea.setText(renderedContent);
        }
    }

    @FXML
    private void handleAddToBatch() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Carga una plantilla primero.");
        } else {
            batchQueue.add(new HashMap<>(collectCurrentTokenValues()));
            batchCountLabel.setText(BATCH_COUNT_PREFIX + batchQueue.size());
            Controller.showAlert("Añadido al lote",
                    "Documento añadido. Total en cola: " + batchQueue.size(), AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleGenerateSingleDocument() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Carga una plantilla primero.");
        } else {
            generateDocuments(selectedTemplate, List.of(collectCurrentTokenValues()));
        }
    }

    @FXML
    private void handleGenerateBatch() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Carga una plantilla primero.");
        } else if (batchQueue.isEmpty()) {
            Controller.showAlert("Cola vacía",
                    "Añade al menos un documento a la cola antes de generar el lote.", AlertType.WARNING);
        } else {
            generateDocuments(selectedTemplate, batchQueue);
            resetBatchQueue();
        }
    }

    private void generateDocuments(DocumentTemplate template, List<Map<String, String>> valuesList) {
        try {
            List<String> generatedPaths = templateManager.generateBatchDocuments(template, valuesList);
            String message = valuesList.size() == 1
                    ? "El documento se guardó en:\n" + generatedPaths.getFirst()
                    : "Se generaron " + generatedPaths.size() + " documento(s) en " + OUTPUT_DIRECTORY_HINT;
            Controller.showAlert("Documento(s) generado(s)", message, AlertType.INFORMATION);
        } catch (ManagerException e) {
            Controller.showAlert("Error al generar", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteTemplate() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Selecciona una plantilla para eliminar.");
        } else {
            deleteSelectedTemplate(selectedTemplate);
        }
    }

    private void deleteSelectedTemplate(DocumentTemplate template) {
        try {
            templateManager.deleteTemplate(template.getTemplateId());
            Controller.showAlert("Plantilla eliminada",
                    "La plantilla \"" + template.getTemplateName() + "\" fue eliminada.", AlertType.INFORMATION);
            refreshSavedTemplatesList();
            tokenValuesContainer.getChildren().clear();
            documentPreviewTextArea.clear();
        } catch (ManagerException e) {
            Controller.showAlert("Error al eliminar", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditTemplate() {
        DocumentTemplate selectedTemplate = getSelectedTemplate();
        if (selectedTemplate == null) {
            showSelectionRequiredAlert("Selecciona una plantilla para editar.");
        } else {
            currentlyEditingTemplate = selectedTemplate;
            templateNameField.setText(selectedTemplate.getTemplateName());
            documentTypeComboBox.setValue(selectedTemplate.getDocumentType());
            templateBodyTextArea.setText(selectedTemplate.getBodyContent());
        }
    }

    private Map<String, String> collectCurrentTokenValues() {
        Map<String, String> collectedValues = new HashMap<>();
        for (Map.Entry<String, TextField> tokenEntry : tokenInputFields.entrySet()) {
            collectedValues.put(tokenEntry.getKey(), tokenEntry.getValue().getText());
        }
        return collectedValues;
    }

    private DocumentTemplate getSelectedTemplate() {
        return savedTemplatesListView.getSelectionModel().getSelectedItem();
    }

    private void showSelectionRequiredAlert(String message) {
        Controller.showAlert("Selección requerida", message, AlertType.WARNING);
    }

    private void resetBatchQueue() {
        batchQueue.clear();
        batchCountLabel.setText(BATCH_COUNT_PREFIX + 0);
    }

    @FXML
    private void handleBackToDashboard() {
        store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}