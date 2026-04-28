package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;

public class NavigationSlice implements SliceReducer {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof NavigationAction navAction)) {
            return currentState;
        }

        var nextNavState = NavigationReducer.reduce(
                currentState.navigationState(),
                navAction
        );

        return currentState.withNavigationState(nextNavState);
    }
}