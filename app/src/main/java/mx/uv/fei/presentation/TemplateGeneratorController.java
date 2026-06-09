package mx.uv.fei.presentation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.DocumentTemplate;
import mx.uv.fei.domain.enums.TemplateToken;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.TemplateManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;

@Component
public class TemplateGeneratorController {

    private static final String TYPE_BITACORA = "Bitácora de Reporte Mensual";
    private static final String TYPE_OFICIO   = "Oficio Oficial";
    private static final String TOKEN_FECHA   = "%FECHA_ACTUAL%";
    private static final String DATE_PATTERN  = "dd/MM/yyyy";

    private static final String MSG_SELECCION_REQUERIDA = "Selección requerida";
    private static final String MSG_CARGA_PRIMERO       = "Carga una plantilla primero.";
    private static final String MSG_CAMPO_REQUERIDO     = "Campo requerido";
    private static final String PREFIX_COLA             = "Documentos en cola: ";

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
    private final AppStore applicationStore;

    private DocumentTemplate currentlyEditingTemplate;
    private final List<Map<String, String>> batchQueue = new ArrayList<>();
    private final Map<String, TextField> tokenInputFields = new HashMap<>();

    @Inject
    public TemplateGeneratorController(TemplateManager templateManager, AppStore applicationStore) {
        this.templateManager = templateManager;
        this.applicationStore = applicationStore;
    }

    @FXML
    public void initialize() {
        setupEditorTab();
        setupUsageTab();
        refreshSavedTemplatesList();
    }

    private void setupEditorTab() {
        documentTypeComboBox.setItems(FXCollections.observableArrayList(TYPE_BITACORA, TYPE_OFICIO));
        documentTypeComboBox.getSelectionModel().selectFirst();

        availableTokensListView.setItems(FXCollections.observableArrayList(TemplateToken.values()));
        availableTokensListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                handleInsertToken(null);
            }
        });
    }

    @FXML
    private void handleInsertToken(ActionEvent event) {
        TemplateToken selectedToken = availableTokensListView.getSelectionModel().getSelectedItem();
        if (selectedToken != null) {
            int caretPosition = templateBodyTextArea.getCaretPosition();
            templateBodyTextArea.insertText(caretPosition, selectedToken.getPlaceholder());
            templateBodyTextArea.requestFocus();
        }
    }

    @FXML
    private void handleClearEditor(ActionEvent event) {
        templateNameField.clear();
        templateBodyTextArea.clear();
        documentTypeComboBox.getSelectionModel().selectFirst();
        currentlyEditingTemplate = null;
    }

    @FXML
    private void handleSaveTemplate(ActionEvent event) {
        String templateName = templateNameField.getText();
        String documentType = documentTypeComboBox.getValue();
        String bodyContent  = templateBodyTextArea.getText();

        boolean isNameEmpty = (templateName == null || templateName.trim().isEmpty());
        boolean isBodyEmpty = (bodyContent == null || bodyContent.trim().isEmpty());

        if (isNameEmpty) {
            Controller.showAlert(MSG_CAMPO_REQUERIDO, "El nombre de la plantilla no puede estar vacío.", AlertType.WARNING);
        } else if (isBodyEmpty) {
            Controller.showAlert(MSG_CAMPO_REQUERIDO, "El cuerpo de la plantilla no puede estar vacío.", AlertType.WARNING);
        } else {
            persistTemplate(templateName.trim(), documentType, bodyContent);
        }
    }

    private void persistTemplate(String templateName, String documentType, String bodyContent) {
        DocumentTemplate templateToSave = buildTemplateFromEditor(templateName, documentType, bodyContent);

        try {
            templateManager.saveTemplate(templateToSave);
            Controller.showAlert("Plantilla guardada",
                    "La plantilla \"" + templateName + "\" se guardó correctamente.",
                    AlertType.INFORMATION);
            handleClearEditor(null);
            refreshSavedTemplatesList();
        } catch (ManagerException e) {
            Controller.showAlert("Error al guardar",
                    "No se pudo guardar la plantilla. " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    private DocumentTemplate buildTemplateFromEditor(String name, String type, String body) {
        DocumentTemplate builtTemplate = new DocumentTemplate();

        if (currentlyEditingTemplate != null) {
            builtTemplate.setTemplateId(currentlyEditingTemplate.getTemplateId());
        }

        builtTemplate.setTemplateName(name);
        builtTemplate.setDocumentType(type);
        builtTemplate.setBodyContent(body);
        builtTemplate.setCreatedByUserName(resolveCurrentCoordinatorUserName());
        builtTemplate.setCreatedAt(LocalDate.now());

        return builtTemplate;
    }

    private String resolveCurrentCoordinatorUserName() {
        String resolvedUserName = "coordinadora";

        try {
            boolean hasSession = applicationStore.getState() != null
                    && applicationStore.getState().sessionState() != null
                    && applicationStore.getState().sessionState().currentUserInSession() != null;

            if (hasSession) {
                resolvedUserName = applicationStore.getState().sessionState().currentUserInSession().getUserName();
            }
        } catch (Exception ignored) {
        }

        return resolvedUserName;
    }

    private void setupUsageTab() {
        batchCountLabel.setText(PREFIX_COLA + 0);
        tokenValuesContainer.getChildren().clear();
    }

    private void refreshSavedTemplatesList() {
        try {
            List<DocumentTemplate> allTemplates = templateManager.loadAllTemplates();
            savedTemplatesListView.setItems(FXCollections.observableArrayList(allTemplates));
        } catch (ManagerException e) {
            Controller.showAlert("Error al cargar",
                    "No se pudieron cargar las plantillas. " + e.getMessage(),
                    AlertType.ERROR);
        }
    }

    @FXML
    private void handleLoadTemplate(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA,
                    "Selecciona una plantilla de la lista antes de cargarla.",
                    AlertType.WARNING);
        } else {
            buildTokenInputFields(selectedTemplate);
            documentPreviewTextArea.clear();
            batchQueue.clear();
            batchCountLabel.setText(PREFIX_COLA + 0);
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
            Label noTokensLabel = new Label("Esta plantilla no contiene tokens reemplazables.");
            noTokensLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-style: italic;");
            tokenValuesContainer.getChildren().add(noTokensLabel);
        }
    }

    private void addTokenInputRow(TemplateToken token) {
        Label tokenLabel = new Label(token.getDescription() + ":");
        tokenLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12px;");

        TextField tokenInput = new TextField();
        tokenInput.setPromptText("Valor para " + token.getPlaceholder());
        tokenInput.setStyle("-fx-background-color: #F3F4F6; -fx-background-radius: 8px; -fx-padding: 6px;");
        HBox.setHgrow(tokenInput, Priority.ALWAYS);

        if (TOKEN_FECHA.equals(token.getPlaceholder())) {
            tokenInput.setText(LocalDate.now().format(DateTimeFormatter.ofPattern(DATE_PATTERN)));
        }

        tokenInputFields.put(token.getPlaceholder(), tokenInput);

        VBox rowContainer = new VBox(4, tokenLabel, tokenInput);
        rowContainer.setPadding(new Insets(0, 0, 8, 0));
        tokenValuesContainer.getChildren().add(rowContainer);
    }

    @FXML
    private void handlePreviewDocument(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA, MSG_CARGA_PRIMERO, AlertType.WARNING);
        } else {
            Map<String, String> currentValues = collectCurrentTokenValues();
            String renderedContent = templateManager.renderTemplate(selectedTemplate, currentValues);
            documentPreviewTextArea.setText(renderedContent);
        }
    }

    @FXML
    private void handleAddToBatch(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA, MSG_CARGA_PRIMERO, AlertType.WARNING);
        } else {
            Map<String, String> currentValues = collectCurrentTokenValues();
            batchQueue.add(new HashMap<>(currentValues));
            batchCountLabel.setText(PREFIX_COLA + batchQueue.size());
            Controller.showAlert("Añadido al lote",
                    "Documento añadido. Total en cola: " + batchQueue.size(),
                    AlertType.INFORMATION);
        }
    }

    @FXML
    private void handleGenerateSingleDocument(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA, MSG_CARGA_PRIMERO, AlertType.WARNING);
        } else {
            Map<String, String> currentValues = collectCurrentTokenValues();
            generateDocuments(selectedTemplate, List.of(currentValues));
        }
    }

    @FXML
    private void handleGenerateBatch(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();

        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA, MSG_CARGA_PRIMERO, AlertType.WARNING);
        } else if (batchQueue.isEmpty()) {
            Controller.showAlert("Cola vacía",
                    "Añade al menos un documento a la cola antes de generar el lote.",
                    AlertType.WARNING);
        } else {
            generateDocuments(selectedTemplate, batchQueue);
            batchQueue.clear();
            batchCountLabel.setText(PREFIX_COLA + 0);
        }
    }

    private void generateDocuments(DocumentTemplate template, List<Map<String, String>> valuesList) {
        try {
            List<String> generatedPaths = templateManager.generateBatchDocuments(template, valuesList);
            String message = valuesList.size() == 1
                    ? "El documento se guardó en:\n" + generatedPaths.getFirst()
                    : "Se generaron " + generatedPaths.size() + " documento(s) en app/documents/generated/";
            Controller.showAlert("Documento(s) generado(s)", message, AlertType.INFORMATION);
        } catch (ManagerException e) {
            Controller.showAlert("Error al generar", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteTemplate(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA,
                    "Selecciona una plantilla para eliminar.",
                    AlertType.WARNING);
        } else {
            deleteSelectedTemplate(selectedTemplate);
        }
    }

    private void deleteSelectedTemplate(DocumentTemplate template) {
        try {
            templateManager.deleteTemplate(template.getTemplateId());
            Controller.showAlert("Plantilla eliminada",
                    "La plantilla \"" + template.getTemplateName() + "\" fue eliminada.",
                    AlertType.INFORMATION);
            refreshSavedTemplatesList();
            tokenValuesContainer.getChildren().clear();
            documentPreviewTextArea.clear();
        } catch (ManagerException e) {
            Controller.showAlert("Error al eliminar", e.getMessage(), AlertType.ERROR);
        }
    }

    @FXML
    private void handleEditTemplate(ActionEvent event) {
        DocumentTemplate selectedTemplate = savedTemplatesListView.getSelectionModel().getSelectedItem();
        if (selectedTemplate == null) {
            Controller.showAlert(MSG_SELECCION_REQUERIDA,
                    "Selecciona una plantilla para editar.",
                    AlertType.WARNING);
        } else {
            currentlyEditingTemplate = selectedTemplate;
            templateNameField.setText(selectedTemplate.getTemplateName());
            documentTypeComboBox.setValue(selectedTemplate.getDocumentType());
            templateBodyTextArea.setText(selectedTemplate.getBodyContent());
        }
    }

    private Map<String, String> collectCurrentTokenValues() {
        Map<String, String> collectedValues = new HashMap<>();

        for (Map.Entry<String, TextField> entry : tokenInputFields.entrySet()) {
            collectedValues.put(entry.getKey(), entry.getValue().getText());
        }

        return collectedValues;
    }

    @FXML
    private void handleBackToDashboard(ActionEvent event) {
        applicationStore.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD));
    }
}
