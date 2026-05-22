package mx.uv.fei.domain.statemachine.actions;

import mx.uv.fei.domain.statemachine.enums.AppSection;
import mx.uv.fei.statemachine.core.Action;

public sealed interface NavigationAction extends Action {
    record GoToSection(AppSection section) implements NavigationAction {}
    record ViewEntityDetails(AppSection section, String entityId) implements NavigationAction {}
}