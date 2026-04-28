package mx.uv.fei.domain.statemachine.actions;

import mx.uv.fei.domain.dto.User;

public sealed interface AuthAction extends Action {
    record AdminDetectionResult(boolean exists) implements AuthAction {
    }

    record AdminCreatedSuccessfully() implements AuthAction {
    }

    record LoginSuccess(User loggedInUser) implements AuthAction {
    }
}