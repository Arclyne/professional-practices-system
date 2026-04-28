package mx.uv.fei.domain.statemachine.state;

public record RootState(
        PractitionerState practitionerState,
        NavigationState navigationState,
        AuthState authState
) {
    public static RootState initialState() {
        return new RootState(
                PractitionerState.initialState(),
                NavigationState.initialState(),
                AuthState.initialState()
        );
    }

    public RootState withPractitionerState(PractitionerState newState) {
        return new RootState(newState, this.navigationState, this.authState);
    }

    public RootState withNavigationState(NavigationState newState) {
        return new RootState(this.practitionerState, newState, this.authState);
    }

    public RootState withAuthState(AuthState newState) {
        return new RootState(this.practitionerState, this.navigationState, newState);
    }
}