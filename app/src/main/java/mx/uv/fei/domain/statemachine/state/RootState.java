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

    public RootState withPractitionerState(PractitionerState nextState) {
        return new RootState(nextState, this.navigationState, this.authenticationState, this.sessionState);
    }

    public RootState withNavigationState(NavigationState nextState) {
        return new RootState(this.practitionerState, nextState, this.authenticationState, this.sessionState);
    }

    public RootState withAuthenticationState(AuthenticatorState nextState) {
        return new RootState(this.practitionerState, this.navigationState, nextState, this.sessionState);
    }

    public RootState withSessionState(SessionState nextState) {
        return new RootState(this.practitionerState, this.navigationState, this.authenticationState, nextState);
    }
}