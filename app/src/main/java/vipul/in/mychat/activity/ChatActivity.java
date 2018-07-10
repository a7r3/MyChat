package vipul.in.mychat.activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import vipul.in.mychat.R;
import vipul.in.mychat.Utils;
import vipul.in.mychat.adapter.MessageAdapter;
import vipul.in.mychat.model.Message;

public class ChatActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private final String TAG = getClass().getSimpleName();
    private final List<Message> msgList = new ArrayList<>();
    String getExtra;
    Boolean friendTyping = false;
    DatabaseReference userDatabaseReference;
    TextView receiverName, receiverLastSeen;
    LinearLayoutManager linearLayoutManager;
    MessageAdapter messageAdapter;
    ImageButton imageButton, emojiButton;
    EmojiconEditText editText;
    View rootView;
    SharedPreferences sharedPreferences;
    InterstitialAd mInterstitialAd;
    RewardedVideoAd mRewardedVideoAd;
    RecyclerView chatRecyclerView;
    Toolbar chat_toolbar;
    String senderUid;
    String myThumb, friendThumb;
    private CircleImageView chatProfileImage;
    private String receiverUid;
    private boolean typingStarted;
    private DatabaseReference mRef, rootDatabaseReference, myReference;
    private View profileBottomSheetView;
    private CircleImageView profileBottomSheetImage;
    private TextView profileBottomSheetName;
    private TextView profileBottomSheetStatus;
    private FloatingActionButton imageSelectorButton;
    private ImageView statusEditorButton;
    private BottomSheetDialog profileBottomSheetDialog;

    ImageButton sendAttach;

    @Override
    protected void onStart() {
        Log.d("WAAHChat", "Activity onStart");
        super.onStart();

        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUid = getIntent().getStringExtra("uid");

        myReference = FirebaseDatabase.getInstance().getReference();

        myReference.keepSynced(true);

//        myReference.child("Users").child(senderUid).child("thumb_pic").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                myThumb = dataSnapshot.getValue(String.class);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });
//
//
//        myReference.child("Users").child(receiverUid).child("thumb_pic").addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                friendThumb = dataSnapshot.getValue(String.class);
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//
//            }
//        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(senderUid).child("isOnline").setValue("true");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("WAAHChat", "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeAd();

        sharedPreferences = getSharedPreferences("userInfo", MODE_PRIVATE);

        sendAttach = findViewById(R.id.attachButton);

        myThumb = sharedPreferences.getString("thumb_pic", "default");
        friendThumb = getIntent().getStringExtra("friendThumb");

        Log.d("THUMBS:-", myThumb.equals(friendThumb) ? "1" : "0");


        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        messageAdapter = new MessageAdapter(msgList, this, myThumb, friendThumb);
        chatRecyclerView = findViewById(R.id.messageList);

        linearLayoutManager = new LinearLayoutManager(this);
        chatRecyclerView.setLayoutManager(linearLayoutManager);
        chatRecyclerView.setAdapter(messageAdapter);

        EmojIconActions emojIcon = new EmojIconActions(this, rootView, editText, emojiButton);
        emojIcon.setIconsIds(R.drawable.ic_keyboard_black_24dp, R.drawable.ic_insert_emoticon_black_24dp);
        emojIcon.ShowEmojIcon();

        emojIcon.setKeyboardListener(new EmojIconActions.KeyboardListener() {
            @Override
            public void onKeyboardOpen() {
                Log.i(TAG, "Keyboard Opened");
                chatRecyclerView.scrollToPosition(msgList.size() - 1);
            }

            @Override
            public void onKeyboardClose() {
                Log.e(TAG, "Keyboard Closed");
            }

        });


        editText.addTextChangedListener(new TextWatcher() {


            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {

                if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() == 1) {

                    Log.d("TYPING", "typingStarted");

                    typingStarted = true;
                    FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverUid).child(senderUid).child("typing").setValue(typingStarted);

                    //send typing started status

                } else if (s.toString().trim().length() == 0 && typingStarted) {

                    Log.d("TYPING", "typingStopped");

                    typingStarted = false;
                    FirebaseDatabase.getInstance().getReference().child("Chats").child(receiverUid).child(senderUid).child("typing").setValue(typingStarted);
                    //send typing stopped status

                }

            }

        });

        rootDatabaseReference = FirebaseDatabase.getInstance().getReference();

        rootDatabaseReference.keepSynced(true);

        getExtra = getIntent().getStringExtra("clicked");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        userDatabaseReference.keepSynced(true);

        chat_toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chat_toolbar);

        chat_toolbar.setTitle(null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        View chatCustomToolbar = LayoutInflater.from(this).inflate(R.layout.chat_app_bar, null);
        chatProfileImage = chatCustomToolbar.findViewById(R.id.chatProfilePicture);

        userDatabaseReference.child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                String image = dataSnapshot.child("profile_pic").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();

                profileBottomSheetName.setText(name);
                profileBottomSheetStatus.setText(status);

                sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
                if (!image.equals("default")) {
                    final Uri imageUri = Uri.parse(image);
                    Picasso.with(ChatActivity.this)
                            .load(imageUri)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(profileBottomSheetImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ChatActivity.this)
                                            .load(imageUri)
                                            .placeholder(R.drawable.ic_person_black_24dp)
                                            .into(profileBottomSheetImage);
                                }
                            });
                    //profileBottomSheetImage.setImageURI(Uri.parse(getIntent().getStringExtra("friendProfilePic")));
                    //chatProfileImage.setImageURI(Uri.parse(getIntent().getStringExtra("friendThumb")));
                    Picasso.with(ChatActivity.this)
                            .load(imageUri)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(chatProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError() {
                                    Picasso.with(ChatActivity.this)
                                            .load(imageUri)
                                            .placeholder(R.drawable.ic_person_black_24dp)
                                            .into(chatProfileImage);
                                }
                            });
                } else {
                    profileBottomSheetImage.setImageResource(R.drawable.ic_person_black_24dp);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        actionBar.setCustomView(chatCustomToolbar);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    Log.d("TAG", "The interstitial wasn't loaded yet.");
                }
                Log.d("Hey", "Hey");
            }
        });

        sendAttach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select a Profile Picture"), 55);
            }
        });


        receiverName = findViewById(R.id.chatPersonName);
        receiverLastSeen = findViewById(R.id.chatLastSeen);
        receiverName.setText(getExtra);

        rootDatabaseReference.child("Chats").child(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(receiverUid).exists())
                    rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("seen").setValue(true);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRef = rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("typing");
        mRef.keepSynced(true);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (dataSnapshot.getValue(Boolean.class)) {
                        receiverLastSeen.setText("Typing...");

                        friendTyping = true;

                    } else {
                        friendTyping = false;
                        watchLastSeen();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        rootDatabaseReference.child("Users").child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                watchLastSeen();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        loadMessages();

        profileBottomSheetView = LayoutInflater.from(this).inflate(R.layout.profile_bottom_sheet, null);

        profileBottomSheetImage = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_image);
        profileBottomSheetName = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_name);
        profileBottomSheetStatus = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_status);
        imageSelectorButton = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_image_select_button);
        imageSelectorButton.setVisibility(View.GONE);

        profileBottomSheetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dialog imgDialog = new Dialog(ChatActivity.this);
                imgDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View myView = LayoutInflater.from(ChatActivity.this).inflate(R.layout.image_dialog_layout
                        , null);
                imgDialog.setContentView(myView);

                ImageView imageView = myView.findViewById(R.id.image_dialog_chat_profile_picture);

                imageView.setImageDrawable(profileBottomSheetImage.getDrawable());

                imgDialog.show();
                //Toast.makeText(ChatActivity.this, "ImageViewer coming soon :P", Toast.LENGTH_LONG).show();
            }
        });

        profileBottomSheetDialog = new BottomSheetDialog(this);
        profileBottomSheetDialog.setCanceledOnTouchOutside(true);
        profileBottomSheetDialog.setCancelable(true);
        profileBottomSheetDialog.setContentView(profileBottomSheetView);
        profileBottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);

        chat_toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileBottomSheetDialog.show();
            }
        });


    }

    public void initializeAd() {
        MobileAds.initialize(this, "ca-app-pub-6712400715312717~1651070161");
        imageButton = findViewById(R.id.imageButton);
        emojiButton = findViewById(R.id.emojiButton);
        editText = findViewById(R.id.editTextEmoji);
        rootView = findViewById(R.id.root_view);

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6712400715312717/2525168130");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        mRewardedVideoAd = MobileAds.getRewardedVideoAdInstance(this);
        mRewardedVideoAd.setRewardedVideoAdListener(this);
        mRewardedVideoAd.loadAd("ca-app-pub-6712400715312717/4390227221",
                new AdRequest.Builder().build());

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                // Code to be executed when an ad finishes loading.
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                super.onAdFailedToLoad(errorCode);
                // Code to be executed when an ad request fails.
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                // Code to be executed when the ad is displayed.
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                // Code to be executed when the user has left the app.
            }

            @Override
            public void onAdClosed() {
                super.onAdClosed();
                // Code to be executed when when the interstitial ad is closed.
            }
        });
    }

    private void loadMessages() {

        // Check for new messages under the Messages node
        rootDatabaseReference.child("Messages").child(senderUid).child(receiverUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Message messages = dataSnapshot.getValue(Message.class);
                if (messages.getFrom().equals(senderUid)) {
                    messages.setFrom("Me");
                } else {
                    messages.setFrom(getExtra);
                }

                messageAdapter.addMessage(messages);

                chatRecyclerView.scrollToPosition(msgList.size() - 1);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                // No we haven't added an Edit functionality yet :P
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                // No Delete functionality too :P :P
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                // Who does that
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Reep
            }
        });


    }

    /**
     * Sends a message to the Recipient
     * Adds a node containing the message's details
     * under both the Sender and Receiver's nodes
     * Message Details : Content, Seen (Indicator), Type (Text/Media), Time at which message was sent
     */
    private void sendMessage() {

        messageAdapter.isSendButtonUsed = true;

        // Get the message from the Input Bar
        String msg = editText.getText().toString();
        Log.d("message", msg);

        // If the message is empty, abort the process
        if (TextUtils.isEmpty(msg))
            return;

        // Message node is to be inserted at two locations
        // The Sender's Message Node
        // The Recipient's Message Node
        String senderMessageReference = "Messages/" + senderUid + "/" + receiverUid;
        String receiverMessageReference = "Messages/" + receiverUid + "/" + senderUid;

        // To push the message details under a node, we've got to give this parent node a name
        String pushId = rootDatabaseReference
                .child(senderMessageReference)
                .push()    // push() does that
                .getKey(); // and returns the Unique Name of the Node

        HashMap<String, Object> messageMap = new HashMap<>();
        messageMap.put("message", msg);
        messageMap.put("seen", false);
        messageMap.put("type", "text");
        messageMap.put("time", ServerValue.TIMESTAMP);
        messageMap.put("from", senderUid);

        HashMap<String, Object> messageUserMap = new HashMap<>();

        // We have to push this same node to both Sender and Receiver's 'Messages' node
        // So the Push ID has to be the same
        messageUserMap.put(senderMessageReference + "/" + pushId, messageMap);
        messageUserMap.put(receiverMessageReference + "/" + pushId, messageMap);

        // Blank out the Input Bar, lets the user know that the sending process has been started
        editText.getText().clear();

        // It's obvious that the Sender has seen the message which was created by him/herself
        rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("seen").setValue(true);
        // Set the latest message of this Chat as this message
        rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("lastMessage").setValue(msg);
        // Set the latest message's timestamp as this message's timestamp
        rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("timestamp").setValue(ServerValue.TIMESTAMP);

        // Until the Receiver doesn't see this message, set it to false
        rootDatabaseReference.child("Chats").child(receiverUid).child(senderUid).child("seen").setValue(false);
        // Set the latest message of this Chat as this message
        rootDatabaseReference.child("Chats").child(receiverUid).child(senderUid).child("lastMessage").setValue(msg);
        // Set the latest message's timestamp as this message's timestamp
        rootDatabaseReference.child("Chats").child(receiverUid).child(senderUid).child("timestamp").setValue(ServerValue.TIMESTAMP);

        // Update this 'Messages' node with the New Message
        rootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "sendMessage : " + databaseError.getMessage());
                    return;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 55 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            final String senderMessageReference = "Messages/" + senderUid + "/" + receiverUid;
            final String receiverMessageReference = "Messages/" + receiverUid + "/" + senderUid;

            DatabaseReference user_message_push = rootDatabaseReference.child("Messages").child(senderUid).child(receiverUid).push();

            final String pushId = user_message_push.getKey();

            StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("message_images").child(pushId+".jpg");

            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()) {
                        String download_url = task.getResult().getDownloadUrl().toString();

                        HashMap<String, Object> messageMap = new HashMap<>();
                        messageMap.put("message", download_url);
                        messageMap.put("seen", false);
                        messageMap.put("type", "image");
                        messageMap.put("time", ServerValue.TIMESTAMP);
                        messageMap.put("from", senderUid);

                        HashMap<String, Object> messageUserMap = new HashMap<>();


                        messageUserMap.put(senderMessageReference + "/" + pushId, messageMap);
                        messageUserMap.put(receiverMessageReference + "/" + pushId, messageMap);

                        rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("seen").setValue(true);
                        // Set the latest message of this Chat as this message
                        rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("lastMessage").setValue("Image");
                        // Set the latest message's timestamp as this message's timestamp
                        rootDatabaseReference.child("Chats").child(senderUid).child(receiverUid).child("timestamp").setValue(ServerValue.TIMESTAMP);

                        // Until the Receiver doesn't see this message, set it to false
                        rootDatabaseReference.child("Chats").child(receiverUid).child(senderUid).child("seen").setValue(false);
                        // Set the latest message of this Chat as this message
                        rootDatabaseReference.child("Chats").child(receiverUid).child(senderUid).child("lastMessage").setValue("Image");
                        // Set the latest message's timestamp as this message's timestamp
                        rootDatabaseReference.child("Chats").child(receiverUid).child(senderUid).child("timestamp").setValue(ServerValue.TIMESTAMP);

                        // Update this 'Messages' node with the New Message
                        rootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                            @Override
                            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                                if (databaseError != null) {
                                    Log.e(TAG, "sendMessage : " + databaseError.getMessage());
                                    return;
                                }
                            }
                        });
                    }
                }
            });
        }

    }

    public void watchLastSeen() {

        if (!friendTyping) {
            mRef = userDatabaseReference.child(receiverUid);
            mRef.child("isOnline").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if ("true".equals(dataSnapshot.getValue(String.class))) {
                        receiverLastSeen.setText("Online");
                    } else {
                        mRef.child("lastSeen").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                long timeStamp = dataSnapshot.getValue(Long.class);
                                Log.d(TAG, "LastSeen : " + timeStamp);
                                receiverLastSeen.setText(Utils.getTimeAgo(timeStamp));
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }


    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRewardedVideoAd.pause(this);
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child("Users").child(senderUid).child("isOnline").setValue("false");
        FirebaseDatabase.getInstance().getReference().child("Users").child(senderUid).child("lastSeen").setValue(ServerValue.TIMESTAMP);
    }

    @Override
    public void onRewarded(RewardItem reward) {
        // Reward the user.
        Toast.makeText(this, "onRewarded! currency: " + reward.getType() + "  amount: " +
                reward.getAmount(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdLeftApplication() {
        //Toast.makeText(this, "onRewardedVideoAdLeftApplication",
        //Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdClosed() {
        Toast.makeText(this, "onRewardedVideoAdClosed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdFailedToLoad(int errorCode) {
        //Toast.makeText(this, "onRewardedVideoAdFailedToLoad", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoCompleted() {

    }

    @Override
    public void onRewardedVideoAdLoaded() {
        //Toast.makeText(this, "onRewardedVideoAdLoaded", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoAdOpened() {
        Toast.makeText(this, "onRewardedVideoAdOpened", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRewardedVideoStarted() {
        Toast.makeText(this, "onRewardedVideoStarted", Toast.LENGTH_SHORT).show();
    }
}
