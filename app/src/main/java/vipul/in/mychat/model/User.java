package vipul.in.mychat.model;

/**
 * Created by vipul on 23/1/18.
 */

// Descriptor Class for a User in this Application
public class User {

    // These variables are present inside the 'Chats' node
    // Has this user saw the last message sent by the current user ?
    private boolean seen;
    // Latest message's timestamp
    private long timestamp;
    // Latest message's content
    private String lastMessage;

    // These variables are present inside the 'Users' node
    // Unique ID of this user - Always Unique for a Google Account
    private String uid;
    // Device Token of this user - Varies from Device to Device
    private String device_token;
    // Is the user Online ?
    private String isOnline;
    // When was this user available online (if the user's Offline) ?
    private Long lastSeen;
    // Name of the User
    private String name;
    // Phone Number of the User
    private String phoneNum;
    // Text status of the user
    private String status;

    public User() {
        super();
    }

    public Long getLastSeen() {
        return lastSeen;
    }

    public void setLastSeen(Long lastSeen) {
        this.lastSeen = lastSeen;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDevice_token() {
        return device_token;
    }

    public void setDevice_token(String device_token) {
        this.device_token = device_token;
    }

    public String getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(String isOnline) {
        this.isOnline = isOnline;
    }
}
