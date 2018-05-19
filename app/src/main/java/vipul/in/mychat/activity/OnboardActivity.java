package vipul.in.mychat.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.hbb20.CountryCodePicker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import vipul.in.mychat.R;
import vipul.in.mychat.adapter.MessageAdapter;
import vipul.in.mychat.model.Message;

public class OnboardActivity extends AppCompatActivity {

    private final int ASK_PHONE_NUMBER = 0;
    private final int ASK_OTP = 1;
    private final int ASK_USER_NAME = 2;
    private final int ASK_STATUS = 3;
    private final int ASK_PROFILE_PICTURE = 4;
    private final int FINAL_DESTINATION = 5;
    private List<Message> onboardConversation = new ArrayList<>();
    private RecyclerView onboardRecyclerView;
    private MessageAdapter messageAdapter;
    private CountryCodePicker countryCodePicker;
    private EditText inputBox;
    private ImageButton emojiButton;
    private ImageButton sendMessageButton;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String originalVerificationId;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private Handler handler = new Handler();

    private int onboardStage = 0;

    // Onboarding would be like a conversation!

    /**
     * Sends a message in the context of the Bot (the other member of this chat)
     *
     * @param message The message to be displayed to the User
     */
    public void sendMessageAsBot(final String message) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                onboardConversation.add(
                        new Message(message, System.currentTimeMillis(), "IntroBot")
                );
                messageAdapter.notifyItemInserted(onboardConversation.size() - 1);
            }
        }, 1000);
    }

    /**
     * Sends a message in the context of the User (who is being Onboarded)
     *
     * @param message The message to be displayed
     */
    public void sendMessageAsUser(String message) {
        // Sending a message from the User makes it look like a Conversation
        onboardConversation.add(
                new Message(message, System.currentTimeMillis(), "Me")
        );
        messageAdapter.notifyItemInserted(onboardConversation.size() - 1);
    }

    public void signInWithPhoneAuthCredential(PhoneAuthCredential phoneAuthCredential) {
        firebaseAuth.signInWithCredential(phoneAuthCredential)
                .addOnSuccessListener(OnboardActivity.this, new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        user = authResult.getUser();
                        onboardStage++;
                        inputBox.setInputType(EditorInfo.TYPE_CLASS_TEXT);
                        sendMessageAsBot("Verification successful");
                        onboardUser();
                    }
                });
    }

    private void verifyOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                120,
                TimeUnit.SECONDS,
                OnboardActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    @Override
                    public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                        signInWithPhoneAuthCredential(phoneAuthCredential);
                    }

                    @Override
                    public void onVerificationFailed(FirebaseException e) {
                        // TODO: Say Please Try again Later
                    }

                    @Override
                    public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        // TODO: Start the Message Timer since we've got acknowledgement of sending OTP request successfully
                        originalVerificationId = s;
                    }
                });

    }

    // Function which sends messages via Bot to the user according to the current Stage
    // Stages:
    // TODO: At Each Stage (or everytime), check for Internet Connectivity
    // 1. Ask for Phone Number
    // 2. OTP Verification
    // 3. Ask for Username
    // 4. Ask for his Status
    // 5. TODO: Profile Picture
    // 6. (Final Destination) Send Onboard Completion Acknowledgement
    //    |-> Open up MainActivity
    private void onboardUser() {
        switch (onboardStage) {
            case ASK_PHONE_NUMBER:
                sendMessageAsBot("Select your Country code, and enter your Mobile Number");
                inputBox.setHint("Enter Mobile Number");
                countryCodePicker.setVisibility(View.VISIBLE);
                countryCodePicker.registerCarrierNumberEditText(inputBox);
                inputBox.setInputType(EditorInfo.TYPE_CLASS_PHONE);
                break;
            case ASK_OTP:
                sendMessageAsBot("Please wait while we receive a verification SMS");
                sendMessageAsBot("You may enter the OTP if received already");
                inputBox.setHint("Enter OTP");
                break;
            case ASK_USER_NAME:
                sendMessageAsBot("What is your name ?");
                inputBox.setHint("Enter your name");
                break;
            case ASK_STATUS:
                sendMessageAsBot("Tell everyone about yourself by typing in your Status!");
                inputBox.setHint("Enter your Status");
                break;
            case ASK_PROFILE_PICTURE:
                // TODO: this
                sendMessageAsBot("Profile Picture can be set once you complete Registration");
                onboardStage++;
                onboardUser();
                break;
            case FINAL_DESTINATION:
                sendMessageAsBot("So our Journey ends here! MyChat would start in a few moments, hold on!");
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        databaseReference.child("phoneNum").setValue(user.getPhoneNumber());
                        databaseReference.child("device_token").setValue(FirebaseInstanceId.getInstance().getToken());
                        // TODO: Move on to the Application
                        startActivity(new Intent(OnboardActivity.this, MainActivity.class));
                        finish();
                    }
                }, 2000);
                break;
        }
    }

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "USER IS NOTNULL");
            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("isOnline").setValue("true");
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child("Users");

            SharedPreferences sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("initialized", "YES");
            mRef.child(mAuth.getCurrentUser().getUid()).child("profile_pic").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    editor.putString("profile_pic", dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mRef.child(mAuth.getCurrentUser().getUid()).child("thumb_pic").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    editor.putString("thumb_pic", dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        countryCodePicker = findViewById(R.id.country_code_picker);
        inputBox = findViewById(R.id.editTextEmoji);
        sendMessageButton = findViewById(R.id.send_message_button);
        emojiButton = findViewById(R.id.emojiButton);
        onboardRecyclerView = findViewById(R.id.onboard_recycler_view);
        // TODO: New Avatars needed
        messageAdapter = new MessageAdapter(onboardConversation, this, "default", "default");
        onboardRecyclerView.setAdapter(messageAdapter);
        onboardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        sendMessageAsBot("Hey there, Welcome to MyChat!");
        sendMessageAsBot("Let's get started");

        onboardUser();


        // The Send button over here acts differently according to the current Onboard Stage
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (onboardStage) {
                    case ASK_PHONE_NUMBER:
                        // If the provided number is not valid
                        if (!countryCodePicker.isValidFullNumber()) {
                            sendMessageAsBot("Please enter a valid mobile number");
                            return;
                        }
                        // Get the number with country code, and the + sign
                        String phoneNumber = countryCodePicker.getFullNumberWithPlus();
                        // Hide the CountryCodePicker
                        countryCodePicker.setVisibility(View.GONE);
                        // No more autoformatting for future messages required
                        countryCodePicker.setNumberAutoFormattingEnabled(false);
                        // Clear the Input Box before OTP verification starts
                        inputBox.getText().clear();
                        // Send the phone number in the User Context
                        sendMessageAsUser(phoneNumber);
                        // This stage is complete, move on to OTP verification stage
                        onboardStage++;
                        // Umm, start off the next stage
                        verifyOtp(phoneNumber);
                        // Onboard the user according to the current stage viz. OTP verification
                        onboardUser();
                        break;
                    case ASK_OTP:
                        // In case the user has manually entered OTP via InputBox
                        // Take it from InputBox
                        // Send it as a message in the user context
                        String verificationCode = inputBox.getText().toString();
                        sendMessageAsUser(inputBox.getText().toString());
                        // Try to authenticate with the OTP Received
                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(originalVerificationId, verificationCode);
                        signInWithPhoneAuthCredential(credential);
                        // Move on to the next stage viz. Asking for User Name
                        onboardStage++;
                        onboardUser();
                        break;
                    case ASK_USER_NAME:
                        // Set the username in the Database
                        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());
                        databaseReference.child("name").setValue(inputBox.getText().toString());
                        // Send this username as a message in the user context
                        sendMessageAsUser(inputBox.getText().toString());
                        // Move on to the next stage viz. Asking for Status
                        onboardStage++;
                        onboardUser();
                        break;
                    case ASK_STATUS:
                        // Set the status in the Database
                        databaseReference.child("status").setValue(inputBox.getText().toString());
                        // Send this status as a message in the user context
                        sendMessageAsUser(inputBox.getText().toString());
                        // Move on to the next stage, viz. Asking for a Profile Picture
                        onboardStage++;
                        onboardUser();
                        break;
                    case ASK_PROFILE_PICTURE:
                        // TODO: Do something about this
                        // Yet to be implemented, set it to default
                        databaseReference.child("profile_pic").setValue("default");
                        databaseReference.child("thumb_pic").setValue("default");
                        break;
                    case FINAL_DESTINATION:
                        // Give me a break; Nothing to do here
                        break;
                }
                // Clear the Input Box on every hit on this button
                inputBox.getText().clear();
            }
        });

    }

}
