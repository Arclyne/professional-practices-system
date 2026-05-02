package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;

public class AuthenticatorSlice implements SliceReducer {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof AuthenticatorAction AuthenticatorAction)) {
            return currentState;
        }

        var nextAuthState = AuthenticatorReducer.reduce(
                currentState.authenticationState(),
                AuthenticatorAction);

        return currentState.withAuthenticationState(nextAuthState);
    }
}