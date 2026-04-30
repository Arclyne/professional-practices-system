package mx.uv.fei.domain.statemachine.state;

public record RootState(
        PractitionerState practitionerState,
        NavigationState navigationState,
        AuthenticatorState authenticationState,
        SessionState sessionState) {
    public static RootState initialState() {
        return new RootState(
                PractitionerState.initialState(),
                NavigationState.initialState(),
                AuthenticatorState.initialState(),
                SessionState.initialState());
    }
}