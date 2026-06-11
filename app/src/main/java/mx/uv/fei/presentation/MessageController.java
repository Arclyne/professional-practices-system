package mx.uv.fei.presentation;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.domain.dto.MessageRequest;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.MessageManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.MessageAction;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.domain.statemachine.state.MessageState;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.presentation.interfaces.IDisposable;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MessageController implements Initializable, IDisposable {

    private static final Logger log = LoggerFactory.getLogger(MessageController.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int MESSAGE_PAGE_SIZE = 50;
    private static final int FIRST_PAGE_OFFSET = 0;

    @FXML private Button navigateToDashboardButton;
    @FXML private Button inboxNavigationButton;
    @FXML private Button composeNavigationButton;
    @FXML private VBox inboxContainer;
    @FXML private VBox composeContainer;
    @FXML private VBox readContainer;
    @FXML private TableView<Message> messagesTableView;
    @FXML private TableColumn<Message, String> senderTableColumn;
    @FXML private TableColumn<Message, String> subjectTableColumn;
    @FXML private TableColumn<Message, String> dateTableColumn;
    @FXML private Button refreshButton;
    @FXML private TextField receiverEmailTextField;
    @FXML private TextField subjectTextField;
    @FXML private TextArea bodyTextArea;
    @FXML private Button sendMessageButton;
    @FXML private Button backToInboxButton;
    @FXML private Label readSubjectLabel;
    @FXML private Label readSenderLabel;
    @FXML private Label readDateLabel;
    @FXML private TextArea readBodyTextArea;
    @FXML private Button sentNavigationButton;
    @FXML private Label inboxTitleLabel;

    private final AppStore store;
    private final MessageManager messageManager;

    private Runnable unsubscribe;
    private ExecutorService executorService;
    private boolean isInboxViewActive = true;

    @Inject
    public MessageController(AppStore store, MessageManager messageManager) {
        this.store = store;
        this.messageManager = messageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (executorService == null || executorService.isShutdown()) {
            executorService = Executors.newSingleThreadExecutor();
        }
        configureTableView();
        setupListeners();
        unsubscribe = store.subscribe(this::onStateChanged);
        fetchMessages();
    }

    @Override
    public void dispose() {
        if (unsubscribe != null) {
            unsubscribe.run();
        }
        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdown();
        }
    }

    private void configureTableView() {
        senderTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSenderName()));
        subjectTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject()));
        dateTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSendDate().format(DATE_FORMATTER)));

        messagesTableView.setRowFactory(_ -> {
            TableRow<Message> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && !row.isEmpty()) {
                    showReadView(row.getItem());
                }
            });
            return row;
        });
    }

    private void setupListeners() {
        sentNavigationButton.setOnAction(_ -> showSentView());
        navigateToDashboardButton.setOnAction(_ -> store.dispatch(new NavigationAction.GoToSection(AppSection.DASHBOARD)));
        refreshButton.setOnAction(_ -> fetchMessages());
        inboxNavigationButton.setOnAction(_ -> showInboxView());
        composeNavigationButton.setOnAction(_ -> showComposeView());
        backToInboxButton.setOnAction(_ -> showInboxView());
        sendMessageButton.setOnAction(_ -> handleSendMessageAction());
    }

    private void showInboxView() {
        isInboxViewActive = true;
        inboxTitleLabel.setText("Bandeja de Entrada");
        senderTableColumn.setText("Remitente");
        showOnlyContainer(inboxContainer);
        fetchMessages();
    }

    private void showSentView() {
        isInboxViewActive = false;
        inboxTitleLabel.setText("Mensajes Enviados");
        senderTableColumn.setText("Destinatario");
        showOnlyContainer(inboxContainer);
        fetchMessages();
    }

    private void showComposeView() {
        showOnlyContainer(composeContainer);
    }

    private void showReadView(Message message) {
        readSubjectLabel.setText(message.getSubject());
        readSenderLabel.setText("De: " + message.getSenderName());
        readDateLabel.setText("Fecha: " + message.getSendDate().format(DATE_FORMATTER));
        readBodyTextArea.setText(message.getBody());
        showOnlyContainer(readContainer);
    }

    private void showOnlyContainer(VBox visibleContainer) {
        setContainerVisibility(inboxContainer, inboxContainer == visibleContainer);
        setContainerVisibility(composeContainer, composeContainer == visibleContainer);
        setContainerVisibility(readContainer, readContainer == visibleContainer);
    }

    private void setContainerVisibility(VBox container, boolean isVisible) {
        container.setVisible(isVisible);
        container.setManaged(isVisible);
    }

    private void fetchMessages() {
        var currentUser = store.getState().sessionState().currentUserInSession();
        if (currentUser == null) {
            log.warn("Se intentó cargar mensajes sin una sesión activa.");
            return;
        }

        int currentUserId = currentUser.getId();
        store.dispatch(new MessageAction.FetchMessages(currentUserId, FIRST_PAGE_OFFSET));

        Task<List<Message>> fetchTask = buildFetchTask(currentUserId);
        fetchTask.setOnSucceeded(_ -> dispatchFetchSuccess(fetchTask.getValue()));
        fetchTask.setOnFailed(_ -> store.dispatch(
                new MessageAction.FetchMessagesFailure(fetchTask.getException().getMessage())));
        executorService.submit(fetchTask);
    }

    private Task<List<Message>> buildFetchTask(int currentUserId) {
        return new Task<>() {
            @Override
            protected List<Message> call() throws ManagerException {
                return isInboxViewActive
                        ? messageManager.getInboxMessages(currentUserId, MESSAGE_PAGE_SIZE, FIRST_PAGE_OFFSET)
                        : messageManager.getSentMessages(currentUserId, MESSAGE_PAGE_SIZE, FIRST_PAGE_OFFSET);
            }
        };
    }

    private void dispatchFetchSuccess(List<Message> retrievedMessages) {
        if (isInboxViewActive) {
            store.dispatch(new MessageAction.FetchInboxSuccess(retrievedMessages));
        } else {
            store.dispatch(new MessageAction.FetchSentSuccess(retrievedMessages));
        }
    }

    private void handleSendMessageAction() {
        String receiverEmail = receiverEmailTextField.getText().trim();
        String subject = subjectTextField.getText().trim();
        String body = bodyTextArea.getText().trim();

        if (receiverEmail.isEmpty() || subject.isEmpty() || body.isEmpty()) {
            Controller.showAlert("Campos Incompletos",
                    "Por favor, complete todos los campos requeridos para enviar el correo.", AlertType.WARNING);
            return;
        }

        try {
            int senderId = store.getState().sessionState().currentUserInSession().getId();
            messageManager.sendMessage(new MessageRequest(senderId, receiverEmail, subject, body));
            Controller.showAlert("Éxito",
                    "El mensaje institucional ha sido enviado correctamente.", AlertType.INFORMATION);
            clearComposeForm();
            showInboxView();
        } catch (ManagerException e) {
            log.error("Error al enviar el mensaje desde el controlador gráfico.", e);
            Controller.showAlert("Error de Envío", e.getMessage(), AlertType.ERROR);
        }
    }

    private void clearComposeForm() {
        receiverEmailTextField.clear();
        subjectTextField.clear();
        bodyTextArea.clear();
    }

    private void onStateChanged(RootState previousState, RootState currentState) {
        Platform.runLater(() -> {
            MessageState messageState = currentState.messageState();
            if (!messageState.isLoading()) {
                List<Message> messages = isInboxViewActive
                        ? messageState.inboxMessagesList()
                        : messageState.sentMessagesList();
                messagesTableView.getItems().setAll(messages);
            }
            if (messageState.errorMessage() != null) {
                log.error("Error en el estado de mensajes: {}.", messageState.errorMessage());
                store.dispatch(new MessageAction.ClearMessageError());
            }
        });
    }
}