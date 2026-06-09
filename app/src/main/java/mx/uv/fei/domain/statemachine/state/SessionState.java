package mx.uv.fei.domain.statemachine.state;

import mx.uv.fei.domain.dto.User;

public record SessionState(User currentUserInSession) {
    public static SessionState initialState() {
        return new SessionState(null);
    }
}
