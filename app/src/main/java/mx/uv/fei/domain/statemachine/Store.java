package mx.uv.fei.domain.statemachine;

import mx.uv.fei.domain.dto.User;
import mx.uv.fei.domain.statemachine.reducers.AuthSlice;
import mx.uv.fei.domain.statemachine.reducers.NavigationSlice;
import mx.uv.fei.domain.statemachine.reducers.SessionSlice;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.reducers.CombineReducers;
import mx.uv.fei.domain.statemachine.reducers.PractitionerSlice;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class Store {
    private static volatile Store storeInstance;
    private RootState applicationState;
    private final CombineReducers rootReducer;
    private final CopyOnWriteArrayList<BiConsumer<RootState, RootState>> stateChangeListeners = new CopyOnWriteArrayList<>();

    private Store() {
        this.applicationState = RootState.initialState();
        this.rootReducer = new CombineReducers(
                new PractitionerSlice(),
                new NavigationSlice(),
                new AuthSlice(),
                new SessionSlice());
    }

    public static Store getInstance() {
        if (storeInstance == null) {
            synchronized (Store.class) {
                if (storeInstance == null) {
                    storeInstance = new Store();
                }
            }
        }
        return storeInstance;
    }

    public Runnable subscribe(BiConsumer<RootState, RootState> listenerToRegister) {
        stateChangeListeners.add(listenerToRegister);
        return () -> stateChangeListeners.remove(listenerToRegister);
    }

    public synchronized void dispatch(Action actionToProcess) {
        RootState previousApplicationState = this.applicationState;
        this.applicationState = rootReducer.reduce(this.applicationState, actionToProcess);

        for (BiConsumer<RootState, RootState> currentListener : stateChangeListeners) {
            currentListener.accept(previousApplicationState, this.applicationState);
        }
    }

    public RootState getState() {
        return applicationState;
    }

    public static User getCurrentUser() {
        return getInstance().getState().sessionState().currentUserInSession();
    }
}