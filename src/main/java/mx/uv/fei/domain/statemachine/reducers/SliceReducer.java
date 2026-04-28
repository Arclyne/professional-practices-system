package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.state.RootState;

public interface SliceReducer {
    RootState reduce(RootState currentState, Action action);
}