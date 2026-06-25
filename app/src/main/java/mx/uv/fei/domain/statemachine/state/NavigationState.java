package mx.uv.fei.domain.statemachine.state;

import mx.uv.fei.domain.statemachine.enums.AppSection;

public record NavigationState(AppSection currentSection) {
    public static NavigationState initialState() {
        return new NavigationState(AppSection.SPLASH_SCREEN);
    }
}
