package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.SessionAction;
import mx.uv.fei.domain.statemachine.state.SessionState;

public class SessionReducer {

    public static SessionState reduce(SessionState currentState, SessionAction action) {
        return switch (action) {
            case SessionAction.LoginSuccess a -> new SessionState(a.loggedInUser());
            case SessionAction.Logout a -> new SessionState(null);
        };
    }
}