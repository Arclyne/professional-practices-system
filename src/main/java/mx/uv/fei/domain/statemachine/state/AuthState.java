package mx.uv.fei.domain.statemachine.state;

import mx.uv.fei.domain.dto.User;

public record AuthState(
        boolean isAdminRegistered,
        boolean isCheckingStatus,
        User currentUser) {
    public static AuthState initialState() {
        return new AuthState(true, true, null);
    }
}