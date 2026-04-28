package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction;

public class PractitionerSlice implements SliceReducer {

    @Override
    public RootState reduce(RootState currentState, Action action) {
        if (!(action instanceof PractitionerAction pAction)) {
            return currentState;
        }

        var nextPractitionerState = PractitionerReducer.reduce(
                currentState.practitionerState(),
                pAction
        );

        return currentState.withPractitionerState(nextPractitionerState);
    }
}