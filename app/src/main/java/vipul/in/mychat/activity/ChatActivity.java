package vipul.in.mychat.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.reward.RewardItem;
import com.google.android.gms.ads.reward.RewardedVideoAd;
import com.google.android.gms.ads.reward.RewardedVideoAdListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import vipul.in.mychat.R;
import vipul.in.mychat.Utils;
import vipul.in.mychat.adapter.MessageAdapter;
import vipul.in.mychat.model.Message;

public class ChatActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private String receiverUid;
    private final String TAG = getClass().getSimpleName();
    private final List<Message> msgList = new ArrayList<>();
    String getExtra;
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

    String myThumb,friendThumb;

    private DatabaseReference mRef, rootDatabaseReference;

    @Override
    protected void onStart() {
        Log.d(TAG, "Activity onStart");
        super.onStart();
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUid = getIntent().getStringExtra("uid");

        DatabaseReference myReference = FirebaseDatabase.getInstance().getReference();

        myReference.child("Users").child(senderUid).child("thumb_pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myThumb = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


        myReference.child("Users").child(receiverUid).child("thumb_pic").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                friendThumb = dataSnapshot.getValue(String.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        FirebaseDatabase.getInstance().getReference().child("Users").child(senderUid).child("isOnline").setValue("true");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("OnCreate", "OnClick");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        initializeAd();

        sharedPreferences = getSharedPreferences("userInfo",MODE_PRIVATE);

        myThumb = sharedPreferences.getString("thumb_pic","default");
        friendThumb = getIntent().getStringExtra("friendThumb");

        Log.d("THUMBS",myThumb+" "+friendThumb);


        receiverUid = getIntent().getStringExtra("uid");
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();



        messageAdapter = new MessageAdapter(msgList, this , myThumb , friendThumb);
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
//                chatRecyclerView.scrollToPosition(msgList.size() - 1);
            }
        });

        rootDatabaseReference = FirebaseDatabase.getInstance().getReference();

        getExtra = getIntent().getStringExtra("clicked");
        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        chat_toolbar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chat_toolbar);

        chat_toolbar.setTitle(null);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        actionBar.setCustomView(R.layout.chat_app_bar);

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

        receiverName = findViewById(R.id.custom_person_name);
        receiverLastSeen = findViewById(R.id.custom_person_lastSeen);
        receiverName.setText(getExtra);
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
                msgList.add(messages);
                messageAdapter.notifyDataSetChanged();
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
     *  under both the Sender and Receiver's nodes
     * Message Details : Content, Seen (Indicator), Type (Text/Media), Time at which message was sent
     */
    private void sendMessage() {

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
        editText.setText("");

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
                }
            }
        });

    }


    public void watchLastSeen() {

        mRef = FirebaseDatabase.getInstance().getReference().child("Users").child(receiverUid);
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
