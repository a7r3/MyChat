package vipul.in.mychat.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import vipul.in.mychat.R;
import vipul.in.mychat.adapter.ViewPagerAdapter;
import vipul.in.mychat.fragment.ChatListFragment;
import vipul.in.mychat.fragment.ContactsFragment;

public class MainActivity extends AppCompatActivity {

    //Button mBtn;
    //TextView mTextView;
    DatabaseReference mRef;
    private FirebaseAuth mAuth;
    private android.support.v4.app.Fragment contacts, chatListFragment;
    private FirebaseUser currentUser;
    private InterstitialAd mInterstitialAd;
    private TabLayout tabLayout;
    private ViewPager viewPager;


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-6712400715312717~1651070161");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");


        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();


            editor.putString("initialized","YES");
            mRef.child(mAuth.getCurrentUser().getUid()).child("profile_pic").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    editor.putString("profile_pic",dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            mRef.child(mAuth.getCurrentUser().getUid()).child("thumb_pic").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    editor.putString("thumb_pic",dataSnapshot.getValue(String.class));
                    editor.apply();
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });


        viewPager = findViewById(R.id.viewPager);
        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6712400715312717/2525168130");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());

        tabLayout = findViewById(R.id.tabLayout);

        viewPager.setOffscreenPageLimit(3);
        tabLayout.setupWithViewPager(viewPager);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                viewPager.setCurrentItem(position, false);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        setupViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        contacts = new ContactsFragment();
        chatListFragment = new ChatListFragment();
//        profileFragment = new ProfileFragment();
        adapter.addFragment(chatListFragment, "CHATS");
        adapter.addFragment(contacts, "CONTACTS");
//        adapter.addFragment(profileFragment,"PROFILE");
        viewPager.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, AuthActivity.class));
            finish();
        } else {

            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("isOnline").setValue("true");
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Tag", "mainActivity_onPause");
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("isOnline").setValue("false");
            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public void onBackPressed() {

        new AlertDialog.Builder(this).setTitle("Exit")
                .setMessage("Press Yes to log-out from this session")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                }).setNegativeButton("No", null).show();
    }

    //    @Override
//    protected void onStop() {
//        super.onStop();
//        Log.d("Tag","mainActivity_onStop");
//        if(currentUser!=null)
//            FirebaseDatabase.getInstance().getReference().child("Users").child(currentUser.getUid()).child("isOnline").setValue(String.valueOf(ServerValue.TIMESTAMP));
//
//    }
}
