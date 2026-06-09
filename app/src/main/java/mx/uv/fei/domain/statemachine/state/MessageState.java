package mx.uv.fei.domain.statemachine.state;

import mx.uv.fei.domain.dto.Message;
import java.util.Collections;
import java.util.List;

public record MessageState(
        boolean isLoading,
        List<Message> inboxMessagesList,
        List<Message> sentMessagesList,
        String errorMessage
) {
    public static MessageState initialState() {
        return new MessageState(false, Collections.emptyList(), Collections.emptyList(), null);
    }
}
