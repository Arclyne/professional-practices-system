package mx.uv.fei.domain.statemachine.reducers;

import mx.uv.fei.domain.statemachine.state.PractitionerState;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction.SearchPractitioners;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction.LoadPractitionersSuccess;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction.PractitionerError;
import mx.uv.fei.domain.statemachine.actions.PractitionerAction.ClearPractitionerError;

public class PractitionerReducer {

    public static PractitionerState reduce(PractitionerState currentState, PractitionerAction action) {
        return switch (action) {

            case SearchPractitioners a -> new PractitionerState(
                    currentState.visiblePractitioners(),
                    currentState.totalRecordsInDb(),
                    a.page(),
                    a.query(),
                    true,
                    null
            );

            case LoadPractitionersSuccess a -> new PractitionerState(
                    a.results(),
                    a.totalCount(),
                    currentState.currentPage(),
                    currentState.currentQuery(),
                    false,
                    null
            );

            case PractitionerError a -> new PractitionerState(
                    currentState.visiblePractitioners(),
                    currentState.totalRecordsInDb(),
                    currentState.currentPage(),
                    currentState.currentQuery(),
                    false,
                    a.message()
            );

            case ClearPractitionerError a -> new PractitionerState(
                    currentState.visiblePractitioners(),
                    currentState.totalRecordsInDb(),
                    currentState.currentPage(),
                    currentState.currentQuery(),
                    currentState.isFetching(),
                    null
            );
        };
    }
}
