package vipul.in.mychat.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import vipul.in.mychat.R;
import vipul.in.mychat.adapter.MessageAdapter;
import vipul.in.mychat.model.Message;
import vipul.in.mychat.util.Constants;
import vipul.in.mychat.util.UtilityMethods;

public class OnboardActivity extends AppCompatActivity {

    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private final int ASK_PHONE_NUMBER = 0;
    private final int ASK_OTP = 1;
    private final int ASK_USER_NAME = 2;
    private final int ASK_STATUS = 3;
    private final int ASK_PROFILE_PICTURE = 4;
    private final int FINAL_DESTINATION = 5;
    private final String TAG = getClass().getSimpleName();
    @BindView(R.id.onboard_recycler_view)
    RecyclerView onboardRecyclerView;
    @BindView(R.id.country_code_picker)
    CountryCodePicker countryCodePicker;
    @BindView(R.id.editTextEmoji)
    EditText inputBox;
    @BindView(R.id.emojiButton)
    ImageButton emojiButton;
    @BindView(R.id.send_message_button)
    ImageButton sendMessageButton;
    @BindView(R.id.onboard_network_status)
    TextView onboardNetworkStatusText;
    private List<Message> onboardConversation = new ArrayList<>();
    private MessageAdapter messageAdapter;
    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private String originalVerificationId;
    private DatabaseReference databaseReference;
    private FirebaseUser user;
    private Handler handler = new Handler();

    // Onboarding would be like a conversation!
    private int onboardStage = 0;
    private Disposable disposable;

    /**
     * Sends a message in the context of the Bot (the other member of this chat)
     *
     * @param message The message to be displayed to the User
     */
    public void sendMessageAsBot(final String message) {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
                onboardRecyclerView.smoothScrollToPosition(onboardConversation.size() - 1);
            }
        }, 500);
        onboardConversation.add(
                new Message(message, System.currentTimeMillis(), "IntroBot")
        );
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
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                messageAdapter.notifyDataSetChanged();
                onboardRecyclerView.smoothScrollToPosition(onboardConversation.size() - 1);
            }
        }, 500);
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
                        databaseReference.child(Constants.FIREBASE_USER_PHONE_NUM).setValue(user.getPhoneNumber());
                        databaseReference.child("device_token").setValue(FirebaseInstanceId.getInstance().getToken());

                        databaseReference.child(Constants.FIREBASE_USER_PROFILE_PIC).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.FIREBASE_USERS_NODE)
                                            .child(user.getUid())
                                            .child(Constants.FIREBASE_USER_PROFILE_PIC)
                                            .setValue(Constants.DEFAULT_PROFILE_PICTURE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                        databaseReference.child(Constants.FIREBASE_USER_THUMB_PIC).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                if (!dataSnapshot.exists()) {
                                    FirebaseDatabase.getInstance().getReference()
                                            .child(Constants.FIREBASE_USERS_NODE)
                                            .child(user.getUid())
                                            .child(Constants.FIREBASE_USER_THUMB_PIC)
                                            .setValue(Constants.DEFAULT_PROFILE_PICTURE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });


                        // TODO: Move on to the Application
                        startActivity(new Intent(OnboardActivity.this, MainActivity.class));
                        finish();
                    }
                }, 2000);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.dispose();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboard);
        ButterKnife.bind(this);

        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Log.d(TAG, "USER IS NOTNULL");
            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(currentUser.getUid()).child(Constants.FIREBASE_USER_IS_ONLINE).setValue("true");
            DatabaseReference mRef = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE);
            mRef.keepSynced(true);

            SharedPreferences sharedPreferences = getSharedPreferences(Constants.SPREF_USER_INFO, Context.MODE_PRIVATE);
            final SharedPreferences.Editor editor = sharedPreferences.edit();

            editor.putString("initialized", "YES");

            mRef.child(mAuth.getCurrentUser().getUid()).child(Constants.FIREBASE_USER_PROFILE_PIC).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    editor.putString(Constants.SPREF_USER_PROFILE_PICTURE, dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            mRef.child(mAuth.getCurrentUser().getUid()).child(Constants.FIREBASE_USER_THUMB_PIC).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    editor.putString(Constants.SPREF_USER_THUMB_PICTURE, dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }

        disposable = Observable.interval(0, 5, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        if (!UtilityMethods.isNetworkAvailable(OnboardActivity.this)) {
                            onboardNetworkStatusText.setText("You are Offline");
                            onboardNetworkStatusText.setBackgroundColor(Color.parseColor("#F44336"));
                        } else {
                            onboardNetworkStatusText.setText("We're connected");
                            onboardNetworkStatusText.setBackgroundColor(Color.parseColor("#4CAF50"));
                        }
                    }
                })
                .subscribe();

        messageAdapter = new MessageAdapter(onboardConversation, this, Constants.DEFAULT_PROFILE_PICTURE, Constants.DEFAULT_PROFILE_PICTURE);
        onboardRecyclerView.setAdapter(messageAdapter);
        onboardRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        sendMessageAsBot("Hey there, Welcome to MyChat!");
        sendMessageAsBot("Let's get started");

        checkAndRequestPermissions();
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
                        // No more auto-formatting for future messages required
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
                        databaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(user.getUid());
                        // Set the username in the Database
                        databaseReference.keepSynced(true);
                        databaseReference.child(Constants.FIREBASE_USER_NAME).setValue(inputBox.getText().toString());
                        // Send this username as a message in the user context
                        sendMessageAsUser(inputBox.getText().toString());
                        // Move on to the next stage viz. Asking for Status
                        onboardStage++;
                        onboardUser();
                        break;
                    case ASK_STATUS:
                        // Set the status in the Database
                        databaseReference.child(Constants.FIREBASE_USER_STATUS).setValue(inputBox.getText().toString());
                        // Send this status as a message in the user context
                        sendMessageAsUser(inputBox.getText().toString());
                        // Move on to the next stage, viz. Asking for a Profile Picture
                        onboardStage++;
                        onboardUser();
                        break;
                    case ASK_PROFILE_PICTURE:
                        // TODO: Do something about this
                        //Yet to be implemented, set it to default
                        databaseReference.child(Constants.FIREBASE_USER_PROFILE_PIC).setValue(Constants.DEFAULT_PROFILE_PICTURE);
                        databaseReference.child(Constants.FIREBASE_USER_THUMB_PIC).setValue(Constants.DEFAULT_PROFILE_PICTURE);
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

    private boolean checkAndRequestPermissions() {
        int permissionReadContacts = ContextCompat.checkSelfPermission(OnboardActivity.this, Manifest.permission.READ_CONTACTS);
        int permissionWriteContacts = ContextCompat.checkSelfPermission(OnboardActivity.this, Manifest.permission.WRITE_CONTACTS);
        int permissionReadStorage = ContextCompat.checkSelfPermission(OnboardActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE);
        int permissionWriteStorage = ContextCompat.checkSelfPermission(OnboardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();
        if (permissionReadContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_CONTACTS);
        }
        if (permissionWriteContacts != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_CONTACTS);
        }
        if (permissionReadStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        while (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(OnboardActivity.this, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d(TAG, "Permission callback called-------");
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {

                Map<String, Integer> perms = new HashMap<>();
                // Initialize the map with both permissions
                perms.put(Manifest.permission.READ_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_CONTACTS, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.READ_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with actual results from user
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);
                    // Check for both permissions
                    if (perms.get(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_CONTACTS) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        Log.d(TAG, "YES");
                    } else {
                        Log.d(TAG, "Some permissions are not granted ask again ");
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
//                        // shouldShowRequestPermissionRationale will return true
                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(OnboardActivity.this, Manifest.permission.READ_CONTACTS)
                                || ActivityCompat.shouldShowRequestPermissionRationale(OnboardActivity.this, Manifest.permission.WRITE_CONTACTS)
                                || ActivityCompat.shouldShowRequestPermissionRationale(OnboardActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                                || ActivityCompat.shouldShowRequestPermissionRationale(OnboardActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("STORAGE and CONTACT permissions required.",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            switch (which) {
                                                case DialogInterface.BUTTON_POSITIVE:
                                                    checkAndRequestPermissions();
                                                    break;
                                                case DialogInterface.BUTTON_NEGATIVE:
                                                    // proceed with logic by disabling the related features or quit the app.
                                                    break;
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(OnboardActivity.this, "Go to settings and enable permissions", Toast.LENGTH_LONG)
                                    .show();
                            //                            //proceed with logic by disabling the related features or quit the app.
                        }
                    }
                }
            }
        }

    }

    private void showDialogOK(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(OnboardActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show();
    }


}
