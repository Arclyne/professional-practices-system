package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;
import mx.uv.fei.domain.statemachine.actions.SessionAction;
import mx.uv.fei.domain.statemachine.state.SessionState;
import mx.uv.fei.statemachine.core.Action;

public class SessionReducer {

    public static SessionState reduce(SessionState currentState, Action action) {

        if (action instanceof AuthenticatorAction.LoginSuccess loginSuccess) {
            return new SessionState(loginSuccess.loggedInUser());
        }

        if (action instanceof SessionAction.LoginSuccess loginSuccess) {
            return new SessionState(loginSuccess.loggedInUser());
        }

        if (action instanceof SessionAction.Logout) {
            return SessionState.initialState();
        }

        return currentState;
    }
}