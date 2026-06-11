package mx.uv.fei.domain.dto;

public record MessageRequest(int senderId, String receiverEmail, String subject, String body) {
}
