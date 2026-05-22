package mx.uv.fei.domain.statemachine.actions;

import mx.uv.fei.domain.dto.Message;
import mx.uv.fei.statemachine.core.Action;
import java.util.List;


public sealed interface MessageAction extends Action {
    record FetchMessages(int receiverId, int page) implements MessageAction {}
    record FetchInboxSuccess(List<Message> messagesList) implements MessageAction {}
    record FetchSentSuccess(List<Message> messagesList) implements MessageAction {}
    record FetchMessagesFailure(String errorMessage) implements MessageAction {}
    record ClearMessageError() implements MessageAction {}
}