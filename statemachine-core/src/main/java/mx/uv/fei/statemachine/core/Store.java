package mx.uv.fei.statemachine.core;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public abstract class Store<S> {
    protected S applicationState;
    protected CombineReducers<S> applicationRootReducer;
    private final CopyOnWriteArrayList<BiConsumer<S, S>> stateChangeListeners = new CopyOnWriteArrayList<>();

    public Store(S initialState, CombineReducers<S> rootReducer) {
        this.applicationState = initialState;
        this.applicationRootReducer = rootReducer;
    }

    public Runnable subscribe(BiConsumer<S, S> listenerToRegister) {
        stateChangeListeners.add(listenerToRegister);
        return () -> stateChangeListeners.remove(listenerToRegister);
    }

    public synchronized void dispatch(Action actionToProcess) {
        S previousApplicationState = this.applicationState;
        this.applicationState = applicationRootReducer.reduce(this.applicationState, actionToProcess);

        for (BiConsumer<S, S> currentListener : stateChangeListeners) {
            currentListener.accept(previousApplicationState, this.applicationState);
        }
    }

    public S getState() {
        return applicationState;
    }
}