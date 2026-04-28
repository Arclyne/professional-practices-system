package mx.uv.fei.domain.statemachine;

import mx.uv.fei.domain.statemachine.reducers.AuthSlice;
import mx.uv.fei.domain.statemachine.reducers.NavigationSlice;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.actions.Action;
import mx.uv.fei.domain.statemachine.reducers.CombineReducers;
import mx.uv.fei.domain.statemachine.reducers.PractitionerSlice;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

public class Store {

    private static volatile Store instance;
    private RootState state;
    private final CombineReducers rootReducer;

    private final CopyOnWriteArrayList<BiConsumer<RootState, RootState>> listeners = new CopyOnWriteArrayList<>();

    private Store() {
        this.state = RootState.initialState();
        this.rootReducer = new CombineReducers(
                new PractitionerSlice(),
                new NavigationSlice(),
                new AuthSlice()
        );
    }

    public static Store getInstance() {
        if (instance == null) {
            synchronized (Store.class) {
                if (instance == null) {
                    instance = new Store();
                }
            }
        }
        return instance;
    }

    public Runnable subscribe(BiConsumer<RootState, RootState> listener) {
        listeners.add(listener);
        return () -> listeners.remove(listener);
    }

    public synchronized void dispatch(Action action) {
        RootState previousState = this.state;

        this.state = rootReducer.reduce(this.state, action);

        for (BiConsumer<RootState, RootState> listener : listeners) {
            listener.accept(this.state, previousState);
        }
    }

    public RootState getState() {
        return state;
    }
}