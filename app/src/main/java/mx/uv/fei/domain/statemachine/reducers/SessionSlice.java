package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.actions.SessionAction;
import mx.uv.fei.domain.statemachine.state.SessionState;

public class SessionSlice {
    public SessionState reduce(SessionState currentSessionState, Action applicationAction) {
        if (applicationAction instanceof SessionAction.LoginSuccess loginSuccessAction) {
            return new SessionState(loginSuccessAction.loggedInUser());
        }

        if (applicationAction instanceof SessionAction.Logout) {
            return new SessionState(null);
        }

        return currentSessionState;
    }
}
