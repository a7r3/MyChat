package vipul.in.mychat.activity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import vipul.in.mychat.R;
import vipul.in.mychat.adapter.ViewPagerAdapter;
import vipul.in.mychat.fragment.ChatListFragment;
import vipul.in.mychat.fragment.ContactsFragment;
import vipul.in.mychat.util.Constants;

public class MainActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();
    private final int GALLERY_PICK = 99;
    //Button mBtn;
    //TextView mTextView;
    DatabaseReference mRef;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private FirebaseAuth mAuth;
    private android.support.v4.app.Fragment contacts, chatListFragment, myProfile;
    private FirebaseUser currentUser;
    private InterstitialAd mInterstitialAd;
    @BindView(R.id.tabLayout)
    TabLayout tabLayout;
    @BindView(R.id.viewPager)
    ViewPager viewPager;
    private BottomSheetDialog profileBottomSheetDialog;
    private BottomSheetDialog settingsBottomSheetDialog;
    @BindView(R.id.main_profile)
    CircleImageView profileView;
    @BindView(R.id.main_settings)
    ImageView settingsView;
    private DatabaseReference databaseReference;
    private View profileBottomSheetView;
    private CircleImageView profileBottomSheetImage;
    private TextView profileBottomSheetName;
    private TextView profileBottomSheetStatus;
    private View settingsBottomSheetView;
    private FloatingActionButton imageSelectorButton;
    private ImageView statusEditorButton;
    private boolean isSnackBarShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        sharedPreferences = getSharedPreferences("userInfo", Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();

        MobileAds.initialize(this, "ca-app-pub-6712400715312717~1651070161");

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId("ca-app-pub-6712400715312717/2525168130");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());


        profileBottomSheetView = LayoutInflater.from(this).inflate(R.layout.profile_bottom_sheet, null);

        profileBottomSheetImage = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_image);
        profileBottomSheetName = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_name);
        profileBottomSheetStatus = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_status);
        imageSelectorButton = profileBottomSheetView.findViewById(R.id.profile_bottom_sheet_image_select_button);

        imageSelectorButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent, "Select a Profile Picture"), GALLERY_PICK);
            }
        });


        profileBottomSheetStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String status_value = profileBottomSheetStatus.getText().toString();

                final android.app.Dialog settingsDialog = new android.app.Dialog(MainActivity.this);

                settingsDialog.getWindow().requestFeature(android.view.Window.FEATURE_NO_TITLE);
                View myView = LayoutInflater.from(MainActivity.this).inflate(R.layout.statusdialog, null);
                settingsDialog.setContentView(myView);

                final android.widget.EditText newStatus = myView.findViewById(R.id.enterStatus);
                Button submitStatus = myView.findViewById(R.id.submitStatus);

                newStatus.setText(status_value);

                submitStatus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!newStatus.getText().toString().equals(null) && !newStatus.getText().toString().equals("")) {
                            FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("status").setValue(newStatus.getText().toString());
                            settingsDialog.dismiss();
                        }
                    }
                });
                settingsDialog.show();
            }
        });

        profileBottomSheetImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Dialog imgDialog = new Dialog(MainActivity.this);
                imgDialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
                View myView = LayoutInflater.from(MainActivity.this).inflate(R.layout.image_dialog_layout
                        , null);
                imgDialog.setContentView(myView);

                final ImageView imageView = myView.findViewById(R.id.image_dialog_chat_profile_picture);

                if ("default".equals(sharedPreferences.getString("profile_pic", "default"))) {
                    imageView.setImageResource(R.drawable.ic_person_black_24dp);
                } else {
                    Picasso.get()
                            .load(sharedPreferences.getString("profile_pic", "default"))
                            .networkPolicy(NetworkPolicy.OFFLINE)
                            .into(imageView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get().load(sharedPreferences.getString("profile_pic", "default")).into(imageView);
                                }
                            });
                }
                imgDialog.show();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        final String curr_uid = user.getUid();

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(curr_uid);
        databaseReference.keepSynced(true);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("profile_pic").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_pic").getValue().toString();

                profileBottomSheetName.setText(name);
                profileBottomSheetStatus.setText(status);

                if (!image.equals("default")) {
                    final Uri imageUri = Uri.parse(sharedPreferences.getString("profile_pic", "default"));
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
                            .into(profileView, new Callback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onError(Exception e) {
                                    Picasso.get()
                                            .load(imageUri)
                                            .placeholder(R.drawable.ic_person_black_24dp)
                                            .into(profileView);
                                }
                            });
                } else {
                    profileBottomSheetImage.setImageResource(R.drawable.ic_person_black_24dp);
                    profileView.setImageResource(R.drawable.ic_person_black_24dp);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        settingsBottomSheetView = LayoutInflater.from(this).inflate(R.layout.settings_bottom_sheet, null);

        profileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileBottomSheetDialog.show();
            }
        });

        profileBottomSheetDialog = new BottomSheetDialog(this);
        profileBottomSheetDialog.setCanceledOnTouchOutside(true);
        profileBottomSheetDialog.setCancelable(true);
        profileBottomSheetDialog.setContentView(profileBottomSheetView);
        profileBottomSheetDialog.getWindow().findViewById(R.id.design_bottom_sheet).setBackgroundColor(Color.TRANSPARENT);

        settingsBottomSheetDialog = new BottomSheetDialog(this);
        settingsBottomSheetDialog.setCanceledOnTouchOutside(true);
        settingsBottomSheetDialog.setCancelable(true);
        settingsBottomSheetDialog.setContentView(settingsBottomSheetView);

        settingsView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                settingsBottomSheetDialog.show();
            }
        });

        viewPager.setOffscreenPageLimit(2);
        tabLayout.setupWithViewPager(viewPager);

        setupViewPager(viewPager);

    }

    private void setupViewPager(ViewPager viewPager) {

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        contacts = new ContactsFragment();
        chatListFragment = new ChatListFragment();
        adapter.addFragment(chatListFragment, "CHATS");
        adapter.addFragment(contacts, "CONTACTS");
        viewPager.setAdapter(adapter);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "mainActivity_onPause");
        if (currentUser != null) {
            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(currentUser.getUid()).child("isOnline").setValue("false");
            FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(currentUser.getUid()).child("lastSeen").setValue(ServerValue.TIMESTAMP);
        }
    }

    @Override
    public void onBackPressed() {
        if (!isSnackBarShown) {
            Snackbar.make(getWindow().getDecorView().getRootView(), "Press back again to exit", Snackbar.LENGTH_LONG).show();
            isSnackBarShown = true;
            Observable.interval(0, 5, TimeUnit.SECONDS)
                    .take(1)
                    .doOnNext(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            isSnackBarShown = false;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io())
                    .subscribe();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();

            Log.d(TAG, imageUri.toString());


            CropImage.activity(imageUri)
                    .setAspectRatio(1, 1)
                    .setMinCropWindowSize(500, 500)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                StorageReference mImageStorage = FirebaseStorage.getInstance().getReference();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                final String current_uid = user.getUid();
                final DatabaseReference mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
                mUserDatabase.keepSynced(true);
                final ProgressDialog mProgressDialog = new ProgressDialog(this);

                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while we upload and process the image.");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                Uri resultUri = result.getUri();

                Log.d(TAG, resultUri.toString());

                File thumb_filePath = new File(resultUri.getPath());

                FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

                String current_user_id = mCurrentUser.getUid();

                Bitmap thumb_bitmap = null;
                try {
                    thumb_bitmap = new Compressor(this)
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                final byte[] thumb_byte = baos.toByteArray();

                final StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbs").child(current_user_id + ".jpg");

                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(final Uri uri) {
                                    UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                    uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
//                                            final String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();
                                            if (thumb_task.isSuccessful()) {
                                                Map<String, Object> update_hashMap = new HashMap<>();
                                                update_hashMap.put("profile_pic", uri.toString());
//                                                update_hashMap.put("thumb_pic", thumb_downloadUrl);
                                                mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mProgressDialog.dismiss();

                                                            editor.putString("profile_pic",uri.toString());
//                                                            editor.putString("thumb_pic", thumb_downloadUrl);
                                                            editor.apply();

                                                            Picasso.get().load(uri).into(profileBottomSheetImage);
                                                            Picasso.get().load(uri).into(profileView);
                                                            //Toast.makeText(getParentFragment().getContext(), "Success Uploading.", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });

                                            } else {
                                                //Toast.makeText(getParentFragment().getContext(), "Error in uploading thumbnail.", Toast.LENGTH_LONG).show();
                                                mProgressDialog.dismiss();
                                            }
                                        }
                                    });
                                }
                            });
                        } else {
                            //Toast.makeText(getParentFragment().getContext(), "Error in uploading.", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
