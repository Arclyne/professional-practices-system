package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.MessageAction;
import mx.uv.fei.domain.statemachine.state.MessageState;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.statemachine.core.Action;
import mx.uv.fei.statemachine.core.SliceReducer;

public class MessageSlice implements SliceReducer<RootState> {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        RootState nextState = currentState;

        if (action instanceof MessageAction messageAction) {
            MessageState currentMessageState = currentState.messageState();
            MessageState nextMessageState = currentMessageState;

            switch (messageAction) {
                case MessageAction.FetchMessages _ :
                    nextMessageState = new MessageState(true, currentMessageState.inboxMessagesList(), currentMessageState.sentMessagesList(), null);
                    break;
                case MessageAction.FetchInboxSuccess successAction:
                    nextMessageState = new MessageState(false, successAction.messagesList(), currentMessageState.sentMessagesList(), null);
                    break;
                case MessageAction.FetchSentSuccess successAction:
                    nextMessageState = new MessageState(false, currentMessageState.inboxMessagesList(), successAction.messagesList(), null);
                    break;
                case MessageAction.FetchMessagesFailure failureAction:
                    nextMessageState = new MessageState(false, currentMessageState.inboxMessagesList(), currentMessageState.sentMessagesList(), failureAction.errorMessage());
                    break;
                case MessageAction.ClearMessageError _ :
                    nextMessageState = new MessageState(currentMessageState.isLoading(), currentMessageState.inboxMessagesList(), currentMessageState.sentMessagesList(), null);
                    break;
                default:
                    break;
            }

            nextState = currentState.withMessageState(nextMessageState);
        }

        return nextState;
    }
}
