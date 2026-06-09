package mx.uv.fei.domain.dto;

import java.time.LocalDateTime;
import java.util.Objects;

public class Message {
    private int messageId;
    private String subject;
    private String body;
    private LocalDateTime sendDate;
    private int senderId;
    private String senderName;
    private int receiverId;

    public Message() {}

    public int getMessageId() { return messageId; }
    public void setMessageId(int messageId) { this.messageId = messageId; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getBody() { return body; }
    public void setBody(String body) { this.body = body; }

    public LocalDateTime getSendDate() { return sendDate; }
    public void setSendDate(LocalDateTime sendDate) { this.sendDate = sendDate; }

    public int getSenderId() { return senderId; }
    public void setSenderId(int senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public int getReceiverId() { return receiverId; }
    public void setReceiverId(int receiverId) { this.receiverId = receiverId; }

    @Override
    public boolean equals(Object obj) {
        boolean isEqual = false;
        if (this == obj) {
            isEqual = true;
        } else if (obj != null && getClass() == obj.getClass()) {
            Message that = (Message) obj;
            isEqual = this.messageId == that.messageId &&
                    this.senderId == that.senderId &&
                    this.receiverId == that.receiverId;
        }
        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messageId, senderId, receiverId);
    }
}