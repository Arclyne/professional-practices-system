package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction;
import mx.uv.fei.statemachine.core.Action;
import mx.uv.fei.statemachine.core.SliceReducer;

public class PractitionerSlice implements SliceReducer<RootState> {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof PractitionerAction practitionerAction)) {
            return currentState;
        }

        var nextPractitionerState = PractitionerReducer.reduce(
                currentState.practitionerState(),
                practitionerAction
        );

        return currentState.withPractitionerState(nextPractitionerState);
    }
}
