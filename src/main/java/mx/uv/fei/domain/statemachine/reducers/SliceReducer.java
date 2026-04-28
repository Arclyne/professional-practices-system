package mx.uv.fei.domain.Interfaces;

import mx.uv.fei.domain.statemachine.state.RootState;

public interface SliceReducer {
    RootState reduce(RootState currentState, Action action);
}