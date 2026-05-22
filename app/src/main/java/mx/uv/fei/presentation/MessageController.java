package mx.uv.fei.presentation;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
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
import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.domain.common.Controller;
import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.domain.exceptions.ManagerException;
import mx.uv.fei.domain.manager.MessageManager;
import mx.uv.fei.domain.statemachine.AppStore;
import mx.uv.fei.domain.statemachine.actions.MessageAction;
import mx.uv.fei.domain.statemachine.state.MessageState;
import mx.uv.fei.domain.statemachine.state.RootState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;


@Component
public class MessageController implements Initializable {

    private static final Logger logger = LoggerFactory.getLogger(MessageController.class);

    @FXML
    private Button navigateToDashboardButton;
    @FXML
    private Button inboxNavigationButton;
    @FXML
    private Button composeNavigationButton;

    @FXML
    private VBox inboxContainer;
    @FXML
    private VBox composeContainer;
    @FXML
    private VBox readContainer;

    @FXML
    private TableView<Message> messagesTableView;
    @FXML
    private TableColumn<Message, String> senderTableColumn;
    @FXML
    private TableColumn<Message, String> subjectTableColumn;
    @FXML
    private TableColumn<Message, String> dateTableColumn;
    @FXML
    private Button refreshButton;

    @FXML
    private TextField receiverEmailTextField;
    @FXML
    private TextField subjectTextField;
    @FXML
    private TextArea bodyTextArea;
    @FXML
    private Button sendMessageButton;

    @FXML
    private Button backToInboxButton;
    @FXML
    private Label readSubjectLabel;
    @FXML
    private Label readSenderLabel;
    @FXML
    private Label readDateLabel;
    @FXML
    private TextArea readBodyTextArea;
    @FXML
    private Button sentNavigationButton;
    @FXML
    private Label inboxTitleLabel;
    private boolean isInboxViewActive = true;

    private final AppStore store;
    private final MessageManager messageManager;
    private Runnable unsubscribe;


    @Inject
    public MessageController(AppStore store, MessageManager messageManager) {
        this.store = store;
        this.messageManager = messageManager;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        configureTableView();
        setupListeners();

        unsubscribe = store.subscribe(this::onStateChanged);
        fetchMessages();
    }

    private void configureTableView() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        senderTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSenderName()));

        subjectTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSubject()));

        dateTableColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getSendDate().format(formatter)));

        messagesTableView.setRowFactory(tableView -> {
            TableRow<Message> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    Message clickedMessage = row.getItem();
                    showReadView(clickedMessage);
                }
            });
            return row;
        });
    }

    private void setupListeners() {
        sentNavigationButton.setOnAction(event -> showSentView());
        navigateToDashboardButton.setOnAction(event -> store.dispatch(new mx.uv.fei.domain.statemachine.actions.NavigationAction.GoToSection(mx.uv.fei.domain.statemachine.enums.AppSection.DASHBOARD)));
        refreshButton.setOnAction(event -> fetchMessages());
        inboxNavigationButton.setOnAction(event -> showInboxView());
        composeNavigationButton.setOnAction(event -> showComposeView());
        backToInboxButton.setOnAction(event -> showInboxView());
        sendMessageButton.setOnAction(event -> handleSendMessageAction());
    }

    private void showInboxView() {
        isInboxViewActive = true;
        inboxTitleLabel.setText("Bandeja de Entrada");
        senderTableColumn.setText("Remitente");

        inboxContainer.setVisible(true);
        inboxContainer.setManaged(true);
        composeContainer.setVisible(false);
        composeContainer.setManaged(false);
        readContainer.setVisible(false);
        readContainer.setManaged(false);

        fetchMessages();
    }

    private void showSentView() {
        isInboxViewActive = false;
        inboxTitleLabel.setText("Mensajes Enviados");
        senderTableColumn.setText("Destinatario");

        inboxContainer.setVisible(true);
        inboxContainer.setManaged(true);
        composeContainer.setVisible(false);
        composeContainer.setManaged(false);
        readContainer.setVisible(false);
        readContainer.setManaged(false);

        fetchMessages();
    }

    private void showComposeView() {
        inboxContainer.setVisible(false);
        inboxContainer.setManaged(false);
        composeContainer.setVisible(true);
        composeContainer.setManaged(true);
        readContainer.setVisible(false);
        readContainer.setManaged(false);
    }

    private void showReadView(Message message) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        readSubjectLabel.setText(message.getSubject());
        readSenderLabel.setText("De: " + message.getSenderName());
        readDateLabel.setText("Fecha: " + message.getSendDate().format(formatter));
        readBodyTextArea.setText(message.getBody());

        inboxContainer.setVisible(false);
        inboxContainer.setManaged(false);
        composeContainer.setVisible(false);
        composeContainer.setManaged(false);
        readContainer.setVisible(true);
        readContainer.setManaged(true);
    }

    private void fetchMessages() {
        var currentUser = store.getState().sessionState().currentUserInSession();

        if (currentUser != null) {
            int currentUserId = currentUser.getId();
            store.dispatch(new MessageAction.FetchMessages(currentUserId, 0));

            javafx.concurrent.Task<java.util.List<Message>> fetchTask = new javafx.concurrent.Task<>() {
                @Override
                protected java.util.List<Message> call() throws Exception {
                    return isInboxViewActive
                            ? messageManager.getInboxMessages(currentUserId, 50, 0)
                            : messageManager.getSentMessages(currentUserId, 50, 0);
                }
            };

            fetchTask.setOnSucceeded(event -> {
                java.util.List<Message> retrievedMessages = fetchTask.getValue();
                if (isInboxViewActive) {
                    store.dispatch(new MessageAction.FetchInboxSuccess(retrievedMessages));
                } else {
                    store.dispatch(new MessageAction.FetchSentSuccess(retrievedMessages));
                }
            });

            fetchTask.setOnFailed(event -> {
                store.dispatch(new MessageAction.FetchMessagesFailure(fetchTask.getException().getMessage()));
            });

            new Thread(fetchTask).start();
        } else {
            logger.warn("Se intentó cargar mensajes sin una sesión activa.");
        }
    }

    private void handleSendMessageAction() {
        String receiverEmail = receiverEmailTextField.getText().trim();
        String subject = subjectTextField.getText().trim();
        String body = bodyTextArea.getText().trim();

        if (!receiverEmail.isEmpty() && !subject.isEmpty() && !body.isEmpty()) {
            try {
                int senderId = store.getState().sessionState().currentUserInSession().getId();
                boolean isSent = messageManager.sendMessage(senderId, receiverEmail, subject, body);

                if (isSent) {
                    Controller.showAlert("Éxito", "El mensaje institucional ha sido enviado correctamente.", AlertType.INFORMATION);
                    clearComposeForm();
                    showInboxView();
                    fetchMessages();
                }
            } catch (ManagerException e) {
                logger.error("Error al despachar el envío de mensaje desde el controlador gráfico", e);
                Controller.showAlert("Error de Envío", e.getMessage(), AlertType.ERROR);
            }
        } else {
            Controller.showAlert("Campos Incompletos", "Por favor, complete todos los campos requeridos para enviar el correo.", AlertType.WARNING);
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
                java.util.List<Message> targetList = isInboxViewActive
                        ? messageState.inboxMessagesList()
                        : messageState.sentMessagesList();

                messagesTableView.getItems().setAll(targetList);
            }

            if (messageState.errorMessage() != null) {
                logger.error(messageState.errorMessage());
                store.dispatch(new MessageAction.ClearMessageError());
            }
        });
    }

    public void dispose() {
        if (unsubscribe != null) {
            unsubscribe.run();
        }
    }
}