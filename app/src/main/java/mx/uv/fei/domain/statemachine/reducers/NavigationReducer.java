package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.NavigationState;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.domain.statemachine.actions.NavigationAction.GoToSection;
import mx.uv.fei.domain.statemachine.actions.NavigationAction.ViewEntityDetails;

public class NavigationReducer {

    public static NavigationState reduce(NavigationState currentState, NavigationAction action) {
        return switch (action) {
            case GoToSection a -> new NavigationState(
                    a.section()
            );

            case ViewEntityDetails a -> new NavigationState(
                    a.section()
            );
        };
    }
}