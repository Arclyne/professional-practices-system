package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.actions.AuthAction;

public class AuthSlice implements SliceReducer {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof AuthAction authAction)) {
            return currentState;
        }

        var nextAuthState = AuthReducer.reduce(
                currentState.authState(),
                authAction
        );

        return currentState.withAuthState(nextAuthState);
    }
}