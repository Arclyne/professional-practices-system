package mx.uv.fei.domain.statemachine.actions;

public sealed interface AuthAction extends Action {
    record AdminDetectionResult(boolean exists) implements AuthAction {}
    record AdminCreatedSuccessfully() implements AuthAction {}
}