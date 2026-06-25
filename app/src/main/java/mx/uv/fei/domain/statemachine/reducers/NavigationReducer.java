package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.NavigationState;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.actions.NavigationAction.GoToSection;

public class NavigationReducer {

    public static NavigationState reduce(NavigationState currentNavigationState, NavigationAction dispatchedAction) {

        return switch (dispatchedAction) {
            case GoToSection goToSectionAction -> new NavigationState(goToSectionAction.section());
        };
    }
}
