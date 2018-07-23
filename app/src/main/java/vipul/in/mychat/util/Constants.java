package vipul.in.mychat.util;

public class Constants {

    // SharedPreferences' keys
    public static final String SPREF_FRIEND_THUMB = "friendThumb";
    public static final String SPREF_USER_THUMB_PICTURE = "thumb_pic";
    public static final String SPREF_USER_INFO = "userInfo";
    public static final String SPREF_USER_PROFILE_PICTURE = "profile_pic";

    // Indicator for 'No Profile Picture'
    public static final String DEFAULT_PROFILE_PICTURE = "default";

    // Intent Extras
    public static final String SELECTED_USER_NAME_EXTRA = "clicked";
    public static final String RECEIVER_UID_EXTRA = "uid";
    public static final String FRIEND_THUMB_PIC_EXTRA = "friendThumb";
    public static final String FRIEND_PROFILE_PIC_EXTRA = "friendProfilePic";

    //////////////
    // FIREBASE //
    //////////////

    // "Users" node
    public static final String FIREBASE_USERS_NODE = "Users";
    public static final String FIREBASE_USER_NAME = "name";
    public static final String FIREBASE_USER_PROFILE_PIC = "profile_pic";
    public static final String FIREBASE_USER_THUMB_PIC = "thumb_pic";
    public static final String FIREBASE_USER_STATUS = "status";
    public static final String FIREBASE_USER_IS_ONLINE = "isOnline";
    public static final String FIREBASE_USER_LASTSEEN = "lastSeen";
    public static final String FIREBASE_USER_PHONE_NUM = "phoneNum";

    // "Chats" node
    public static final String FIREBASE_CHATS_NODE = "Chats";
    public static final String FIREBASE_CHATS_TYPING = "typing";
    public static final String FIREBASE_CHATS_TIMESTAMP = "timestamp";

    // "Messages" node
    public static final String FIREBASE_MESSAGES_NODE = "Messages";

    // "Friends" node
    public static final String FIREBASE_FRIENDS_NODE = "Friends";
    public static final String FIREBASE_FRIENDS_NAME = "name";
}
