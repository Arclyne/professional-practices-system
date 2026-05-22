package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.state.NavigationState;
import mx.uv.fei.domain.statemachine.actions.NavigationAction;
import mx.uv.fei.statemachine.core.Action;
import mx.uv.fei.statemachine.core.SliceReducer;

public class NavigationSlice implements SliceReducer<RootState> {

    @Override
    public RootState reduce(RootState currentRootState, Action dispatchedAction) {
        RootState resolvedRootState = currentRootState;

        if (dispatchedAction instanceof NavigationAction navigationActionToProcess) {
            NavigationState nextNavigationState = NavigationReducer.reduce(
                    currentRootState.navigationState(),
                    navigationActionToProcess
            );

            resolvedRootState = currentRootState.withNavigationState(nextNavigationState);
        }

        return resolvedRootState;
    }
}