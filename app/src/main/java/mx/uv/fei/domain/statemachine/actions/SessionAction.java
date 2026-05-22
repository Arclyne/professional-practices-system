package mx.uv.fei.domain.statemachine.actions;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.statemachine.core.Action;

public sealed interface SessionAction extends Action {
    record LoginSuccess(User loggedInUser) implements SessionAction {
    }

    record Logout() implements SessionAction {
    }
}