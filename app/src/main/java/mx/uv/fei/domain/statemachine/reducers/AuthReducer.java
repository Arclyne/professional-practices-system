package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.AuthAction;
import mx.uv.fei.domain.statemachine.state.AuthenticatorState;

public class AuthReducer {

    public static AuthenticatorState reduce(AuthenticatorState currentState, AuthAction action) {
        return switch (action) {

            case AuthAction.AdminDetectionResult a -> new AuthenticatorState(
                    a.exists(),
                    false,
                    currentState.currentUser());

            case AuthAction.AdminCreatedSuccessfully a -> new AuthenticatorState(
                    true,
                    false,
                    currentState.currentUser());

            case AuthAction.LoginSuccess a -> new AuthenticatorState(
                    currentState.isAdminRegistered(),
                    currentState.isCheckingStatus(),
                    a.loggedInUser());
        };
    }
}