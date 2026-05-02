package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.state.RootState;

public class SessionSlice implements SliceReducer {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        var nextSessionState = SessionReducer.reduce(
                currentState.sessionState(),
                action);

        return currentState.withSessionState(nextSessionState);
    }
}