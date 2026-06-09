package mx.uv.fei.domain.statemachine.state;

public record AuthenticatorState(
        boolean isAdministratorRegistered,
        boolean isCheckingStatus) {
    public static AuthenticatorState

            initialState() {
        return new AuthenticatorState(true, true);
    }
}
