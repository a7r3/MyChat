package vipul.in.mychat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

import java.util.concurrent.TimeUnit;

import vipul.in.mychat.R;

public class AuthActivity extends AppCompatActivity {

    private EditText phoneNumber;
    private EditText otpNumber;

    private ProgressBar progressBar;
    private Button mBtn;

    private int btnType = 0;
    private EditText enterName;
    private FirebaseUser user;
    private FirebaseAuth mAuth;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        enterName = findViewById(R.id.enterName);
        phoneNumber = findViewById(R.id.phoneNumber);
        otpNumber = findViewById(R.id.otp);
        progressBar = findViewById(R.id.pBar);
        mBtn = findViewById(R.id.mBtn);

        mAuth = FirebaseAuth.getInstance();

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (btnType == 0) {
                    progressBar.setVisibility(View.VISIBLE);
                    phoneNumber.setEnabled(false);
                    mBtn.setEnabled(false);

                    String phoneNum = phoneNumber.getText().toString();

                    Log.d("Phone Number:", phoneNum);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNum,
                            120,
                            TimeUnit.SECONDS,
                            AuthActivity.this,
                            new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                @Override
                                public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {
                                    signInWithPhoneAuthCredential(phoneAuthCredential);
                                }

                                @Override
                                public void onVerificationFailed(FirebaseException e) {

                                }

                                @Override
                                public void onCodeSent(String s, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                    mVerificationId = s;
                                    mResendToken = forceResendingToken;

                                    phoneNumber.setVisibility(View.INVISIBLE);
                                    progressBar.setVisibility(View.INVISIBLE);

                                    btnType = 1;
                                    otpNumber.setVisibility(View.VISIBLE);
                                    mBtn.setEnabled(true);
                                    mBtn.setText("VERIFY OTP");

                                }
                            }
                    );
                } else if (btnType == 1) {

                    mBtn.setEnabled(false);
                    progressBar.setVisibility(View.VISIBLE);

                    String verificationCode = otpNumber.getText().toString();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, verificationCode);

                    signInWithPhoneAuthCredential(credential);

                } else {


                    DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Users").child(user.getUid());

                    reference.child("name").setValue(enterName.getText().toString());
                    reference.child("phoneNum").setValue(user.getPhoneNumber());
                    reference.child("device_token").setValue(FirebaseInstanceId.getInstance().getToken());

                    startActivity(new Intent(AuthActivity.this, MainActivity.class));
                    finish();
                }

            }
        });

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()) {
                            enterName.setVisibility(View.VISIBLE);
                            otpNumber.setVisibility(View.GONE);
                            mBtn.setEnabled(true);
                            mBtn.setText("Register");
                            btnType = 3;

                            user = task.getResult().getUser();

                        } else {

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {

                            }

                        }

                    }
                });
    }

}
