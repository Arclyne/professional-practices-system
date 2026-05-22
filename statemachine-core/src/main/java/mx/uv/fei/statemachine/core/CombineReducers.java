package mx.uv.fei.statemachine.core;

import java.util.List;

public class CombineReducers<S> {
    private final List<SliceReducer<S>> reducers;

    @SafeVarargs
    public CombineReducers(SliceReducer<S>... reducers) {
        this.reducers = List.of(reducers);
    }

    public S reduce(S currentState, Action action) {
        S nextState = currentState;
        for (SliceReducer<S> reducer : reducers) {
            nextState = reducer.reduce(nextState, action);
        }
        return nextState;
    }
}