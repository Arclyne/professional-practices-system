package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.AuthenticatorAction;
import mx.uv.fei.domain.statemachine.state.AuthenticatorState;

public class AuthenticatorReducer {

    public static AuthenticatorState reduce(AuthenticatorState currentState, AuthenticatorAction action) {
        return switch (action) {

            case AuthenticatorAction.AdminDetectionResult a -> new AuthenticatorState(
                    a.exists(),
                    false);

            case AuthenticatorAction.AdminCreatedSuccessfully a -> new AuthenticatorState(
                    true,
                    false);

            case AuthenticatorAction.LoginSuccess a -> currentState;
        };
    }
}