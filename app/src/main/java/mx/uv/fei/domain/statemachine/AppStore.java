package mx.uv.fei.domain.statemachine;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.statemachine.core.Store;
import mx.uv.fei.statemachine.core.CombineReducers;
import mx.uv.fei.domain.statemachine.state.RootState;
import mx.uv.fei.domain.statemachine.reducers.PractitionerSlice;
import mx.uv.fei.domain.statemachine.reducers.NavigationSlice;
import mx.uv.fei.domain.statemachine.reducers.AuthenticatorSlice;
import mx.uv.fei.domain.statemachine.reducers.SessionSlice;
import mx.uv.fei.domain.statemachine.reducers.MessageSlice;

@Component
@Keep
public class AppStore extends Store<RootState> {

    @Inject
    public AppStore() {
        super(
                RootState.initialState(),
                new CombineReducers<>(
                        new PractitionerSlice(),
                        new NavigationSlice(),
                        new AuthenticatorSlice(),
                        new SessionSlice(),
                        new MessageSlice()
                )
        );
    }
}
