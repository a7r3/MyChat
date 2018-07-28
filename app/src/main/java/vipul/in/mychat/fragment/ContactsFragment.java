package vipul.in.mychat.fragment;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import vipul.in.mychat.R;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.activity.ImageDialogActivity;
import vipul.in.mychat.adapter.ContactsAdapter;
import vipul.in.mychat.model.User;
import vipul.in.mychat.util.Constants;
import vipul.in.mychat.util.MarginDividerItemDecoration;


public class ContactsFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();
    DatabaseReference friendsDatabase;
    private DatabaseReference userDatabaseReference;
    private ContactsAdapter adapter;
    private List<User> userList;
    private String currUid;
    private RecyclerView contactsRecyclerView;
    private HashMap<String, String> phoneToNameMap;

    public void fetch_data() {

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE);
        userDatabaseReference.keepSynced(true);

        Cursor phones = getContext().getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null,
                null,
                null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC");

        phoneToNameMap = new HashMap<>();

        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replace(" ", "");
            phoneNumber = phoneNumber.replace("-", "");
            if (phoneNumber.charAt(0) != '+') {
                phoneNumber = "+91" + phoneNumber;
            }
            phoneToNameMap.put(phoneNumber, name);
        }

        phones.close();

        userDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {
                User contact = dataSnapshot.getValue(User.class);
                final String phoneNum = dataSnapshot.child(Constants.FIREBASE_USER_PHONE_NUM).getValue(String.class);

                if (!phoneToNameMap.containsKey(phoneNum)) return;

                contact.setName(phoneToNameMap.get(phoneNum));
                contact.setUid(dataSnapshot.getKey());
                userList.add(contact);
                adapter.notifyItemInserted(userList.size() - 1);
                final String friendUid = dataSnapshot.getKey();
                friendsDatabase = FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_FRIENDS_NODE);
                friendsDatabase.keepSynced(true);
                friendsDatabase.child(currUid).child(friendUid).child(Constants.FIREBASE_USER_NAME).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {
                        if (!ds.exists()) {
                            FirebaseDatabase.getInstance().getReference()
                                    .child(Constants.FIREBASE_FRIENDS_NODE)
                                    .child(currUid)
                                    .child(friendUid)
                                    .child(Constants.FIREBASE_FRIENDS_NAME)
                                    .setValue(phoneToNameMap.get(phoneNum));
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, String s) {
                User contact = dataSnapshot.getValue(User.class);
                ListIterator<User> it = userList.listIterator();
                while (it.hasNext()) {
                    if (it.next().getPhoneNum().equals(contact.getPhoneNum())) {
                        int index = it.nextIndex();
                        String tempName = userList.get(index - 1).getName();
                        userList.remove(index - 1);
                        contact.setName(tempName);
                        contact.setUid(dataSnapshot.getKey());
                        userList.add(index - 1, contact);
                        adapter.notifyItemChanged(index - 1);
                        break;
                    }
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child(Constants.FIREBASE_USERS_NODE).child(currUid).child(Constants.FIREBASE_USER_IS_ONLINE).setValue("true");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        userList = new ArrayList<>();

        adapter = new ContactsAdapter(getActivity(), rootView.getContext(), userList);

        adapter.setOnItemClickListener(new ContactsAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View v, User contacts) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(Constants.SELECTED_USER_NAME_EXTRA, contacts.getName());
                intent.putExtra(Constants.RECEIVER_UID_EXTRA, contacts.getUid());
                intent.putExtra(Constants.FRIEND_THUMB_PIC_EXTRA, contacts.getThumb_pic());
                intent.putExtra(Constants.FRIEND_PROFILE_PIC_EXTRA, contacts.getProfile_pic());
                Log.d(TAG, "Key: " + contacts.getUid());
                startActivity(intent);
            }
        });

        adapter.setOnThumbnailClickListener(new ContactsAdapter.OnThumbnailClickListener() {
            @Override
            public void onThumbnailClicked(View v, User contacts) {
                Intent imageDialogIntent = new Intent(getActivity(), ImageDialogActivity.class);
                imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, contacts.getProfile_pic());
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_NAME_EXTRA, contacts.getName());
                imageDialogIntent.putExtra(Constants.RECEIVER_UID_EXTRA, contacts.getUid());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(getActivity(), v, "image_transition");

                getActivity().startActivity(imageDialogIntent, options.toBundle());
                getActivity().overridePendingTransition(0, 0);

            }
        });

        contactsRecyclerView = rootView.findViewById(R.id.contacts_recyclerView);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        contactsRecyclerView.setHasFixedSize(true);
        MarginDividerItemDecoration itemDecoration = new MarginDividerItemDecoration(getContext());
        contactsRecyclerView.addItemDecoration(itemDecoration);
        contactsRecyclerView.setAdapter(adapter);

        fetch_data();

        currUid = FirebaseAuth.getInstance().getUid();
        return rootView;

    }

    public class SaveFile extends AsyncTask<Void, Void, Void> {

        String downloadUrl;
        File file;

        public SaveFile(String downloadUrl, File file) {
            this.downloadUrl = downloadUrl;
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(downloadUrl);
                if (file.createNewFile()) {
                    file.createNewFile();
                }

                InputStream is = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] BYTE = bos.toByteArray();
                OutputStream ous;
                ous = new FileOutputStream(file);
                ous.write(BYTE);
                ous.close();
                is.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

//    /**
//     * Saves Profile Pictures / Thumbnails of the User to Internal/External Storage
//     *
//     * @param contact The User whose profile picture has to be saved
//     */
//    public void saveProfilePictures(User contact) {
//        if (sharedPreferencesThumbLive.getString(contact.getUid(), "null").equals("null")) {
//            editorThumbLive.putString(contact.getUid(), contact.getThumb_pic());
//            editorThumbLive.apply();
//
//            if (contact.getThumb_pic().equals("default")) {
//                editorThumb.putString(contact.getUid(), "default");
//                editorThumb.apply();
//            } else {
//                String thumbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyChat/thumbnails";
//                File thumbDir = new File(thumbPath);
//                if (!thumbDir.exists()) {
//                    thumbDir.mkdirs();
//                }
//                File thumb_pic_local = new File(thumbPath, contact.getUid() + ".jpeg");
//                new SaveFile(contact.getThumb_pic(), thumb_pic_local).execute();
//
//                editorThumb.putString(contact.getUid(), Uri.fromFile(thumb_pic_local).toString());
//                editorThumb.apply();
//
//            }
//
//        } else if (sharedPreferencesThumbLive.getString(contact.getUid(), "null").equals("default")) {
//            editorThumbLive.putString(contact.getUid(), contact.getThumb_pic());
//            editorThumbLive.apply();
//
//            editorThumb.putString(contact.getUid(), "default");
//            editorThumb.apply();
//        } else if (!sharedPreferencesThumbLive.getString(contact.getUid(), "null").equals(contact.getThumb_pic())) {
//            editorThumbLive.putString(contact.getUid(), contact.getThumb_pic());
//            editorThumbLive.apply();
//
//            if (contact.getThumb_pic().equals("default")) {
//                editorThumb.putString(contact.getUid(), "default");
//                editorThumb.apply();
//            } else {
//
//                String thumbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyChat/thumbnails";
//                File thumbDir = new File(thumbPath);
//                if (!thumbDir.exists()) {
//                    thumbDir.mkdirs();
//                }
//                File thumb_pic_local = new File(thumbPath, contact.getUid() + ".jpeg");
//                new SaveFile(contact.getThumb_pic(), thumb_pic_local).execute();
//
//                editorThumb.putString(contact.getUid(), Uri.fromFile(thumb_pic_local).toString());
//                editorThumb.apply();
//            }
//
//        }
//
//        if (sharedPreferencesPicLive.getString(contact.getUid(), "null").equals("null")) {
//            editorPicLive.putString(contact.getUid(), contact.getProfile_pic());
//            editorPicLive.apply();
//
//            if (contact.getProfile_pic().equals("default")) {
//                editorPic.putString(contact.getUid(), "default");
//                editorPic.apply();
//            } else {
//                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyChat/profile_pics";
//                File picDir = new File(picPath);
//                if (!picDir.exists()) {
//                    picDir.mkdirs();
//                }
//                File profile_pic_local = new File(picPath, contact.getUid() + ".jpeg");
//                new SaveFile(contact.getProfile_pic(), profile_pic_local).execute();
//
//                editorPic.putString(contact.getUid(), Uri.fromFile(profile_pic_local).toString());
//                editorPic.apply();
//            }
//        } else if (sharedPreferencesPicLive.getString(contact.getUid(), "null").equals("default")) {
//            editorPicLive.putString(contact.getUid(), contact.getProfile_pic());
//            editorPicLive.apply();
//
//            editorPic.putString(contact.getUid(), "default");
//            editorPic.apply();
//        } else if (!sharedPreferencesPicLive.getString(contact.getUid(), "null").equals(contact.getProfile_pic())) {
//            editorPicLive.putString(contact.getUid(), contact.getProfile_pic());
//            editorPicLive.apply();
//
//            if (contact.getProfile_pic().equals("default")) {
//                editorPic.putString(contact.getUid(), "default");
//                editorPic.apply();
//            } else {
//                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/MyChat/profile_pics";
//                File picDir = new File(picPath);
//                if (!picDir.exists()) {
//                    picDir.mkdirs();
//                }
//                File profile_pic_local = new File(picPath, contact.getUid() + ".jpeg");
//                new SaveFile(contact.getProfile_pic(), profile_pic_local).execute();
//
//                editorPic.putString(contact.getUid(), Uri.fromFile(profile_pic_local).toString());
//                editorPic.apply();
//            }
//        }
//    }


}