package vipul.in.mychat.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, "ca-app-pub-6712400715312717~1651070161");
        mAuth = FirebaseAuth.getInstance();
        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
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

        //mBtn = findViewById(R.id.query);
        //mTextView = findViewById(R.id.list);
        //mRef = FirebaseDatabase.getInstance().getReference().child("Users");

        /*mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()) {
                            String k = ds.child("phoneNum").getValue(String.class);
                            Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,null,null, null);
                            while (phones.moveToNext())
                            {
                                String name=phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                                String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                phoneNumber = phoneNumber.replace(" ","");
                                Log.d(name,phoneNumber+""+k);
                                if(k.contains(phoneNumber)) {
                                    Log.d("Matched","Matched");
                                    mTextView.setText(mTextView.getText().toString()+"\n"+name+": "+k);
                                    break;
                                }
                            }
                            phones.close();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }
        });*/
        setupViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        contacts = new ContactsFragment();
        chatListFragment = new ChatListFragment();
        adapter.addFragment(chatListFragment, "CHATS");
        adapter.addFragment(contacts, "CONTACTS");
        //adapter.addFragment(contacts,"CONTACTS");
        //adapter.addFragment(contacts,"CONTACTS");
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
