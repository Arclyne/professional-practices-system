package mx.uv.fei.domain.statemachine;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.reducers.AuthenticatorSlice;
import mx.uv.fei.domain.statemachine.reducers.CombineReducers;
import mx.uv.fei.domain.statemachine.reducers.NavigationSlice;
import mx.uv.fei.domain.statemachine.reducers.PractitionerSlice;
import mx.uv.fei.domain.statemachine.reducers.SessionSlice;
import mx.uv.fei.domain.statemachine.reducers.SliceReducer;
import mx.uv.fei.domain.statemachine.state.RootState;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

@Component
@Keep
public class Store {

    private RootState applicationState;
    private final CombineReducers applicationRootReducer;
    private final CopyOnWriteArrayList<BiConsumer<RootState, RootState>> stateChangeListeners;

    @Inject
    public Store() {
        this.applicationState = RootState.initialState();
        this.applicationRootReducer = new CombineReducers(
                (SliceReducer) new PractitionerSlice(),
                new NavigationSlice(),
                new AuthenticatorSlice(),
                new SessionSlice());
        this.stateChangeListeners = new CopyOnWriteArrayList<>();
    }

    public Runnable subscribe(BiConsumer<RootState, RootState> listenerToRegister) {
        stateChangeListeners.add(listenerToRegister);

        Runnable unsubscriptionAction = () -> stateChangeListeners.remove(listenerToRegister);

        return unsubscriptionAction;
    }

    public synchronized void dispatch(Action actionToProcess) {
        RootState previousApplicationState = this.applicationState;
        this.applicationState = applicationRootReducer.reduce(this.applicationState, actionToProcess);

        for (BiConsumer<RootState, RootState> currentListener : stateChangeListeners) {
            currentListener.accept(previousApplicationState, this.applicationState);
        }
    }

    public RootState getState() {
        return applicationState;
    }
}