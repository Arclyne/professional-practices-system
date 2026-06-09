package mx.uv.fei.domain.statemachine;

import mx.uv.fei.config.annotation.etiquette.Component;
import mx.uv.fei.config.annotation.etiquette.Inject;
import mx.uv.fei.config.annotation.etiquette.Keep;
import mx.uv.fei.domain.dto.User;

@Component
@Keep
public class SessionFacade {

    private final AppStore store;

    @Inject
    public SessionFacade(AppStore store) {
        this.store = store;
    }

    public User getCurrentUser() {
        return store.getState().sessionState().currentUserInSession();
    }

    public boolean isAuthenticated() {
        return getCurrentUser() != null;
    }
}
