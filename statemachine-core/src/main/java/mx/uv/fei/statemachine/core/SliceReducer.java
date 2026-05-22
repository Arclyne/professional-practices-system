package mx.uv.fei.statemachine.core;

public interface SliceReducer<S> {
    S reduce(S currentState, Action action);
}