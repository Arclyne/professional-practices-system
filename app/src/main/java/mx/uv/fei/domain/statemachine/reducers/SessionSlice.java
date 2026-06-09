package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.statemachine.core.Action;
import mx.uv.fei.statemachine.core.SliceReducer;

public class SessionSlice implements SliceReducer<RootState> {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        var nextSessionState = SessionReducer.reduce(
                currentState.sessionState(),
                action);

        return currentState.withSessionState(nextSessionState);
    }
}
