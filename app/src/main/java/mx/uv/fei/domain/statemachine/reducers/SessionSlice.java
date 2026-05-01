package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.actions.SessionAction;
import mx.uv.fei.domain.statemachine.state.RootState;

public class SessionSlice implements SliceReducer {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof SessionAction sessionAction)) {
            return currentState;
        }

        var nextSessionState = SessionReducer.reduce(
                currentState.sessionState(),
                sessionAction);

        return currentState.withSessionState(nextSessionState);
    }
}