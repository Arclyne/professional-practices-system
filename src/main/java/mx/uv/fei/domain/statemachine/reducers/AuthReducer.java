package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.AuthAction;
import mx.uv.fei.domain.statemachine.state.AuthState;

public class AuthReducer {

    public static AuthState reduce(AuthState currentState, AuthAction action) {
        return switch (action) {

            case AuthAction.AdminDetectionResult a -> new AuthState(
                    a.exists(),
                    false,
                    currentState.currentUsername()
            );

            case AuthAction.AdminCreatedSuccessfully a -> new AuthState(
                    true,
                    false,
                    currentState.currentUsername()
            );
        };
    }
}