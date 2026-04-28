package mx.uv.fei.domain.statemachine.state;

public record AuthState(
        boolean isAdminRegistered,
        boolean isCheckingStatus,
        String currentUsername
) {
    public static AuthState initialState() {
        return new AuthState(false, true, null);
    }
}