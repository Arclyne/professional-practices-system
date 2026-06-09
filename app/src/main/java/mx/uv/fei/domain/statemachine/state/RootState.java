package mx.uv.fei.domain.statemachine.state;

public record RootState(
        PractitionerState practitionerState,
        NavigationState navigationState,
        AuthenticatorState authenticationState,
        SessionState sessionState,
        MessageState messageState
) {

    public static RootState initialState() {
        return new RootState(
                PractitionerState.initialState(),
                NavigationState.initialState(),
                AuthenticatorState.initialState(),
                SessionState.initialState(),
                MessageState.initialState()
        );
    }

    public RootState withPractitionerState(PractitionerState nextState) {
        return new RootState(nextState, this.navigationState, this.authenticationState, this.sessionState, this.messageState);
    }

    public RootState withNavigationState(NavigationState nextState) {
        return new RootState(this.practitionerState, nextState, this.authenticationState, this.sessionState, this.messageState);
    }

    public RootState withAuthenticationState(AuthenticatorState nextState) {
        return new RootState(this.practitionerState, this.navigationState, nextState, this.sessionState, this.messageState);
    }

    public RootState withSessionState(SessionState nextState) {
        return new RootState(this.practitionerState, this.navigationState, this.authenticationState, nextState, this.messageState);
    }

    public RootState withMessageState(MessageState nextState) {
        return new RootState(this.practitionerState, this.navigationState, this.authenticationState, this.sessionState, nextState);
    }
}
