package mx.uv.fei.domain.statemachine.state;

import mx.uv.fei.domain.dto.Practitioner;
import java.util.List;

public record PractitionerState(
        List<Practitioner> visiblePractitioners,
        int totalRecordsInDb,
        int currentPage,
        String currentQuery,
        boolean isFetching,
        String errorMessage
) {
    public static PractitionerState initialState() {
        return new PractitionerState(
                List.of(),
                0,
                1,
                "",
                false,
                null
        );
    }
}
