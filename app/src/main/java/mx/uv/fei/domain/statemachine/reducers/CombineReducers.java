package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import java.util.List;

public class CombineReducers {

    private final List<SliceReducer> reducers;

    public CombineReducers(SliceReducer... reducers) {
        this.reducers = List.of(reducers);
    }

    public RootState reduce(RootState currentState, Action action) {
        RootState nextState = currentState;

        for (SliceReducer reducer : reducers) {
            nextState = reducer.reduce(nextState, action);
        }

        return nextState;
    }
}