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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
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
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import vipul.in.mychat.R;
import vipul.in.mychat.adapter.MessageAdapter;
import vipul.in.mychat.model.Message;
import vipul.in.mychat.util.Constants;

public class ChatActivity extends AppCompatActivity implements RewardedVideoAdListener {

    private final String TAG = getClass().getSimpleName();
    private final List<Message> msgList = new ArrayList<>();
    TextView receiverName;
    TextView receiverLastSeen;
    @BindView(R.id.imageButton)
    ImageButton imageButton;
    @BindView(R.id.emojiButton)
    ImageButton emojiButton;
    @BindView(R.id.editTextEmoji)
    EmojiconEditText editText;
    @BindView(R.id.root_view)
    View rootView;
    @BindView(R.id.messageList)
    RecyclerView chatRecyclerView;
    @BindView(R.id.chat_toolbar)
    Toolbar chat_toolbar;
    @BindView(R.id.attachButton)
    ImageButton sendAttach;
    @BindView(R.id.chat_activity_move_to_bottom)
    FloatingActionButton moveToBottomButton;
    CircleImageView chatProfileImage;
    LinearLayoutManager linearLayoutManager;
    // Profile Bottom Sheet
    View profileBottomSheetView;
    CircleImageView profileBottomSheetImage;
    TextView profileBottomSheetName;
    TextView profileBottomSheetStatus;
    FloatingActionButton imageSelectorButton;
    BottomSheetDialog profileBottomSheetDialog;
    private String receiverUid;
    private boolean typingStarted;
    private DatabaseReference mRef, rootDatabaseReference, myReference;
    private SharedPreferences sharedPreferences;
    private InterstitialAd mInterstitialAd;
    private RewardedVideoAd mRewardedVideoAd;
    private String receiverNameExtra;
    private Boolean friendTyping = false;
    private DatabaseReference userDatabaseReference;
    private String senderUid;
    private String myThumb, friendThumb;
    private MessageAdapter messageAdapter;

    @Override
    protected void onStart() {
        Log.d(TAG, "Activity onStart");
        super.onStart();

        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        receiverUid = getIntent().getStringExtra(Constants.RECEIVER_UID_EXTRA);

        myReference = FirebaseDatabase.getInstance().getReference();

        myReference.keepSynced(true);

//        myReference.child(Constants.FIREBASE_USERS_NODE).child(senderUid).child(Constants.SPREF_USER_THUMB_PICTURE).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                myThumb = dataSnapshot.getValue(String.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });
//
//
//        myReference.child(Constants.FIREBASE_USERS_NODE).child(receiverUid).child(Constants.SPREF_USER_THUMB_PICTURE).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                friendThumb = dataSnapshot.getValue(String.class);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//
//            }
//        });

        FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(senderUid).child("isOnline").setValue("true");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        initializeAd();

        sharedPreferences = getSharedPreferences(Constants.SPREF_USER_INFO, MODE_PRIVATE);

        myThumb = sharedPreferences.getString(Constants.SPREF_USER_THUMB_PICTURE, Constants.DEFAULT_PROFILE_PICTURE);
        friendThumb = getIntent().getStringExtra(Constants.SPREF_FRIEND_THUMB);

        Log.d(TAG, "Thumbs: " + (myThumb.equals(friendThumb) ? "1" : "0"));


        receiverUid = getIntent().getStringExtra(Constants.RECEIVER_UID_EXTRA);
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();


        messageAdapter = new MessageAdapter(msgList, this, myThumb, friendThumb);

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
                Log.d(TAG, "Keyboard Closed");
            }

        });


        editText.addTextChangedListener(new TextWatcher() {

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            public void afterTextChanged(Editable s) {

                if (!TextUtils.isEmpty(s.toString()) && s.toString().trim().length() == 1) {

                    Log.d(TAG, "typingStarted");

                    typingStarted = true;
                    FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child(Constants.FIREBASE_CHATS_TYPING).setValue(typingStarted);

                    //send typing started status

                } else if (s.toString().trim().length() == 0 && typingStarted) {

                    Log.d(TAG, "typingStopped");

                    typingStarted = false;
                    FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child("typing").setValue(typingStarted);
                    //send typing stopped status

                }

            }

        });

        chatRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // If the last visible item in the list is the second-last item
                // Show the Move-To-Last-Message FAB
                // If the last visible item in the list is the last item itself
                // Hide the Move-To-Last-Message FAB
                if (messageAdapter.getItemCount() - 2 == linearLayoutManager.findLastVisibleItemPosition())
                    moveToBottomButton.show();
                else if (messageAdapter.getItemCount() - 1 == linearLayoutManager.findLastVisibleItemPosition())
                    moveToBottomButton.hide();
            }
        });

        chatRecyclerView.setHasFixedSize(false);

        // Pressing this button would smooth scroll the message list to the last message
        moveToBottomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chatRecyclerView.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
        rootDatabaseReference = FirebaseDatabase.getInstance().getReference();

        rootDatabaseReference.keepSynced(true);

        receiverNameExtra = getIntent().getStringExtra(Constants.SELECTED_USER_NAME_EXTRA);

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE);
        userDatabaseReference.keepSynced(true);

        View chatCustomToolbar = LayoutInflater.from(this).inflate(R.layout.chat_app_bar, null);
        chatProfileImage = chatCustomToolbar.findViewById(R.id.chatProfilePicture);
        receiverName = chatCustomToolbar.findViewById(R.id.chatPersonName);
        receiverLastSeen = chatCustomToolbar.findViewById(R.id.chatLastSeen);
        userDatabaseReference.child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child(Constants.FIREBASE_USER_NAME).getValue().toString();
                String image = dataSnapshot.child(Constants.FIREBASE_USER_PROFILE_PIC).getValue().toString();
                String status = dataSnapshot.child(Constants.FIREBASE_USER_STATUS).getValue().toString();

                profileBottomSheetName.setText(name);
                profileBottomSheetStatus.setText(status);

                sharedPreferences = getSharedPreferences(Constants.SPREF_USER_INFO, Context.MODE_PRIVATE);
                if (!image.equals(Constants.DEFAULT_PROFILE_PICTURE)) {
                    final Uri imageUri = Uri.parse(image);
                    Picasso.get()
                            .load(imageUri)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(profileBottomSheetImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(imageUri)
                                            .placeholder(R.drawable.ic_person_black_24dp)
                                            .into(profileBottomSheetImage);
                                }
                            });
                    Picasso.get()
                            .load(imageUri)
                            .placeholder(R.drawable.ic_person_black_24dp)
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(chatProfileImage, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(imageUri)
                                            .placeholder(R.drawable.ic_person_black_24dp)
                                            .into(chatProfileImage);
                                }
                            });
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        setSupportActionBar(chat_toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowCustomEnabled(true);
            getSupportActionBar().setCustomView(chatCustomToolbar);
        }

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage();
                if (mRewardedVideoAd.isLoaded()) {
                    mRewardedVideoAd.show();
                } else {
                    Log.d(TAG, "The interstitial wasn't loaded yet.");
                }
                Log.d(TAG, "Hey");
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
        receiverName.setText(receiverNameExtra);

        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.child(receiverUid).exists())
                    rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child("seen").setValue(true);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        mRef = rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child("typing");
        mRef.keepSynced(true);

        mRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    if (Objects.requireNonNull(dataSnapshot.getValue(Boolean.class))) {
                        receiverLastSeen.setText("Typing...");

                        friendTyping = true;

                    } else {
                        friendTyping = false;
                        watchLastSeen();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        rootDatabaseReference.child(Constants.FIREBASE_USERS_NODE).child(receiverUid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                watchLastSeen();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
        rootDatabaseReference.child(Constants.FIREBASE_MESSAGES_NODE).child(senderUid).child(receiverUid).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                Message messages = dataSnapshot.getValue(Message.class);
                if (messages.getFrom().equals(senderUid)) {
                    messages.setFrom("Me");
                } else {
                    messages.setFrom(receiverNameExtra);
                }

                messageAdapter.addMessage(messages);

                chatRecyclerView.scrollToPosition(msgList.size() - 1);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                // No we haven't added an Edit functionality yet :P
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                // No Delete functionality too :P :P
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {
                // Who does that
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
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
        Log.d(TAG, msg);

        // If the message is empty, abort the process
        if (TextUtils.isEmpty(msg))
            return;

        // Message node is to be inserted at two locations
        // The Sender's Message Node
        // The Recipient's Message Node
        String senderMessageReference = Constants.FIREBASE_MESSAGES_NODE + "/" + senderUid + "/" + receiverUid;
        String receiverMessageReference = Constants.FIREBASE_MESSAGES_NODE + "/" + receiverUid + "/" + senderUid;

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
        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child("seen").setValue(true);
        // Set the latest message of this Chat as this message
        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child("lastMessage").setValue(msg);
        // Set the latest message's timestamp as this message's timestamp
        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child("timestamp").setValue(ServerValue.TIMESTAMP);

        // Until the Receiver doesn't see this message, set it to false
        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child("seen").setValue(false);
        // Set the latest message of this Chat as this message
        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child("lastMessage").setValue(msg);
        // Set the latest message's timestamp as this message's timestamp
        rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child("timestamp").setValue(ServerValue.TIMESTAMP);

        // Update this 'Messages' node with the New Message
        rootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "sendMessage : " + databaseError.getMessage());
                    return;
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 55 && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            if (imageUri == null) {
                Toast.makeText(this, "No Image Selected", Toast.LENGTH_SHORT).show();
                return;
            }

            final String senderMessageReference = Constants.FIREBASE_MESSAGES_NODE + "/" + senderUid + "/" + receiverUid;
            final String receiverMessageReference = Constants.FIREBASE_MESSAGES_NODE + "/" + receiverUid + "/" + senderUid;

            DatabaseReference user_message_push = rootDatabaseReference.child(Constants.FIREBASE_MESSAGES_NODE).child(senderUid).child(receiverUid).push();

            final String pushId = user_message_push.getKey();

            final StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("message_images").child(pushId + ".jpg");

            storageReference.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                HashMap<String, Object> messageMap = new HashMap<>();
                                messageMap.put("message", uri.toString());
                                messageMap.put("seen", false);
                                messageMap.put("type", "image");
                                messageMap.put("time", ServerValue.TIMESTAMP);
                                messageMap.put("from", senderUid);

                                HashMap<String, Object> messageUserMap = new HashMap<>();


                                messageUserMap.put(senderMessageReference + "/" + pushId, messageMap);
                                messageUserMap.put(receiverMessageReference + "/" + pushId, messageMap);

                                rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child(Constants.FIREBASE_CHATS_SEEN).setValue(true);
                                // Set the latest message of this Chat as this message
                                rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child(Constants.FIREBASE_CHATS_LAST_MESSAGE).setValue("Image");
                                // Set the latest message's timestamp as this message's timestamp
                                rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(senderUid).child(receiverUid).child(Constants.FIREBASE_CHATS_TIMESTAMP).setValue(ServerValue.TIMESTAMP);

                                // Until the Receiver doesn't see this message, set it to false
                                rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child(Constants.FIREBASE_CHATS_SEEN).setValue(false);
                                // Set the latest message of this Chat as this message
                                rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child(Constants.FIREBASE_CHATS_LAST_MESSAGE).setValue("Image");
                                // Set the latest message's timestamp as this message's timestamp
                                rootDatabaseReference.child(Constants.FIREBASE_CHATS_NODE).child(receiverUid).child(senderUid).child(Constants.FIREBASE_CHATS_TIMESTAMP).setValue(ServerValue.TIMESTAMP);

                                // Update this 'Messages' node with the New Message
                                rootDatabaseReference.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@NonNull DatabaseError databaseError, DatabaseReference databaseReference) {
                                        if (databaseError != null)
                                            Log.e(TAG, "sendMessage : " + databaseError.getMessage());
                                    }
                                });
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
            mRef.child(Constants.FIREBASE_USER_IS_ONLINE).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if ("true".equals(dataSnapshot.getValue(String.class))) {
                        receiverLastSeen.setText("Online");
                    } else {
                        mRef.child(Constants.FIREBASE_USER_LASTSEEN).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                long timeStamp = Objects.requireNonNull(dataSnapshot.getValue(Long.class));
                                Log.d(TAG, "LastSeen : " + timeStamp);
                                receiverLastSeen.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

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
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRewardedVideoAd.pause(this);
        senderUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(senderUid).child(Constants.FIREBASE_USER_IS_ONLINE).setValue("false");
        FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(senderUid).child(Constants.FIREBASE_USER_LASTSEEN).setValue(ServerValue.TIMESTAMP);
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
