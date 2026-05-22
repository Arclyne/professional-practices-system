package mx.uv.fei.domain.statemachine.actions;

import mx.uv.fei.domain.dto.Practitioner;
import mx.uv.fei.statemachine.core.Action;

import java.util.List;

public sealed interface PractitionerAction extends Action {
    record SearchPractitioners(String query, int page) implements PractitionerAction {}

    record LoadPractitionersSuccess(List<Practitioner> results, int totalCount) implements PractitionerAction {}

    record PractitionerError(String message) implements PractitionerAction {}

    record ClearPractitionerError() implements PractitionerAction {}
}