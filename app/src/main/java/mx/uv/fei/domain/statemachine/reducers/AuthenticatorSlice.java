package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;
import mx.uv.fei.statemachine.core.Action;
import mx.uv.fei.statemachine.core.SliceReducer;

public class AuthenticatorSlice implements SliceReducer<RootState> {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof AuthenticatorAction authenticatorAction)) {
            return currentState;
        }

        var nextAuthState = AuthenticatorReducer.reduce(
                currentState.authenticationState(),
                authenticatorAction);

        return currentState.withAuthenticationState(nextAuthState);
    }
}