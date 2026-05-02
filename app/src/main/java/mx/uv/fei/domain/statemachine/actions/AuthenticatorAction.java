package mx.uv.fei.domain.statemachine.actions;

import mx.uv.fei.domain.dto.User;

public sealed interface AuthenticatorAction extends Action {
    record AdminDetectionResult(boolean exists) implements AuthenticatorAction {
    }

    record AdminCreatedSuccessfully() implements AuthenticatorAction {
    }

    record LoginSuccess(User loggedInUser) implements AuthenticatorAction {
    }
}