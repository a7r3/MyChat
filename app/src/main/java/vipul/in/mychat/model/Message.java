package vipul.in.mychat.model;

/**
 * Created by vipul on 22/1/18.
 */

public class Message {

    // UID of the Message Sender
    private String from;
    // Content of the Message
    private String message;
    // Status indicator - Whether the Receiver has seen it or not
    private boolean seen;
    // Sent time in milliseconds
    private long time;
    // Type of the message
    // Currently only "text" (Standard Text) is supported
    private String type;
    private boolean isLastMessage = false;

    public Message(String message, long time, String from) {
        this.message = message;
        this.time = time;
        this.from = from;
        this.type = "text";
    }

    public void setLastMessage(boolean lastMessage) {
        isLastMessage = lastMessage;
    }

    public boolean isLastMessage() {
        return isLastMessage;
    }

    public Message() {
        super();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
