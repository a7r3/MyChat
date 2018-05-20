package vipul.in.mychat.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import vipul.in.mychat.MarginDividerItemDecoration;
import vipul.in.mychat.R;
import vipul.in.mychat.adapter.ContactsAdapter;
import vipul.in.mychat.model.User;


public class ContactsFragment extends Fragment {

    DatabaseReference mRef;
    private ContactsAdapter adapter;
    private List<User> userList;
    private Activity activity;
    private DatabaseReference userDatabaseReference;
    private Context context;
    private String currUid;
    private RecyclerView contactsRecyclerView;
    private static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;

    SharedPreferences sharedPreferencesThumb;
    SharedPreferences.Editor editorThumb;

    SharedPreferences sharedPreferencesPic;
    SharedPreferences.Editor editorPic;

    SharedPreferences sharedPreferencesThumbLive;
    SharedPreferences.Editor editorThumbLive;

    SharedPreferences sharedPreferencesPicLive;
    SharedPreferences.Editor editorPicLive;

    private static HashMap sortByValues(HashMap map) {
        List list = new LinkedList(map.entrySet());
        // Defined Custom Comparator here
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o1)).getValue())
                        .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // Here I am copying the sorted list in HashMap
        // using LinkedHashMap to preserve the insertion order
        HashMap sortedHashMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }


    public void fetch_data() {

        mRef = FirebaseDatabase.getInstance().getReference().child("Users");
        mRef.keepSynced(true);
        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);
        HashMap<String, String> hm = new HashMap<String, String>();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replace(" ", "");
            phoneNumber = phoneNumber.replace("-", "");
            if (phoneNumber.charAt(0) != '+') {
                phoneNumber = "+91" + phoneNumber;
            }
            hm.put(phoneNumber, name);
        }

        final HashMap hashMap2 = sortByValues(hm);
        Log.d("All contacts: ", hashMap2.toString());
        phones.close();

        Iterator iterator = hashMap2.entrySet().iterator();
        while (iterator.hasNext()) {
            final Map.Entry record = (Map.Entry) iterator.next();
            mRef.addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    User contact = dataSnapshot.getValue(User.class);
                    String k = dataSnapshot.child("phoneNum").getValue(String.class);



                    if (k.equals(record.getKey().toString())) {
                        contact.setName(record.getValue().toString());
                        contact.setUid(dataSnapshot.getKey());
                        userList.add(contact);
                        final String frndUid = dataSnapshot.getKey();
                        FirebaseDatabase.getInstance().getReference().child("Friends").child(currUid).child(frndUid).child("name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot ds) {
                                if(!ds.exists()) {
                                    FirebaseDatabase.getInstance().getReference().child("Friends").child(currUid).child(frndUid).child("name").setValue(record.getValue().toString());
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });

                        if(sharedPreferencesThumbLive.getString(contact.getUid(),"null").equals("null")) {
                            editorThumbLive.putString(contact.getUid(),contact.getThumb_pic());
                            editorThumbLive.apply();

                            if(contact.getThumb_pic().equals("default")) {
                                editorThumb.putString(contact.getUid(),"default");
                                editorThumb.apply();
                            }
                            else {

                                String thumbPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyChat/thumbnails";
                                File thumbDir = new File(thumbPath);
                                if(!thumbDir.exists()) {
                                    thumbDir.mkdirs();
                                }
                                File thumb_pic_local = new File(thumbPath,contact.getUid()+".jpeg");
                                new SaveFile(contact.getThumb_pic(),thumb_pic_local).execute();

                                editorThumb.putString(contact.getUid(), Uri.fromFile(thumb_pic_local).toString());
                                editorThumb.apply();

                            }
                        }

                        else if(sharedPreferencesThumbLive.getString(contact.getUid(),"null").equals("default")) {
                            editorThumbLive.putString(contact.getUid(),contact.getThumb_pic());
                            editorThumbLive.apply();

                            editorThumb.putString(contact.getUid(),"default");
                            editorThumb.apply();
                        }

                        else if(!sharedPreferencesThumbLive.getString(contact.getUid(),"null").equals(contact.getThumb_pic())) {
                            editorThumbLive.putString(contact.getUid(),contact.getThumb_pic());
                            editorThumbLive.apply();

                            if(contact.getThumb_pic().equals("default")) {
                                editorThumb.putString(contact.getUid(),"default");
                                editorThumb.apply();
                            }
                            else {

                                String thumbPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyChat/thumbnails";
                                File thumbDir = new File(thumbPath);
                                if(!thumbDir.exists()) {
                                    thumbDir.mkdirs();
                                }
                                File thumb_pic_local = new File(thumbPath,contact.getUid()+".jpeg");
                                new SaveFile(contact.getThumb_pic(),thumb_pic_local).execute();

                                editorThumb.putString(contact.getUid(), Uri.fromFile(thumb_pic_local).toString());
                                editorThumb.apply();
                            }

                        }

                        if(sharedPreferencesPicLive.getString(contact.getUid(),"null").equals("null")) {
                            editorPicLive.putString(contact.getUid(),contact.getProfile_pic());
                            editorPicLive.apply();

                            if(contact.getProfile_pic().equals("default")) {
                                editorPic.putString(contact.getUid(),"default");
                                editorPic.apply();
                            }
                            else {
                                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyChat/profile_pics";
                                File picDir = new File(picPath);
                                if(!picDir.exists()) {
                                    picDir.mkdirs();
                                }
                                File profile_pic_local = new File(picPath,contact.getUid()+".jpeg");
                                new SaveFile(contact.getProfile_pic(),profile_pic_local).execute();

                                editorPic.putString(contact.getUid(), Uri.fromFile(profile_pic_local).toString());
                                editorPic.apply();
                            }
                        }
                        else if(sharedPreferencesPicLive.getString(contact.getUid(),"null").equals("default")) {
                            editorPicLive.putString(contact.getUid(),contact.getProfile_pic());
                            editorPicLive.apply();

                            editorPic.putString(contact.getUid(),"default");
                            editorPic.apply();
                        }
                        else if(!sharedPreferencesPicLive.getString(contact.getUid(),"null").equals(contact.getProfile_pic())) {
                            editorPicLive.putString(contact.getUid(),contact.getProfile_pic());
                            editorPicLive.apply();

                            if(contact.getProfile_pic().equals("default")) {
                                editorPic.putString(contact.getUid(),"default");
                                editorPic.apply();
                            }
                            else {
                                String picPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/MyChat/profile_pics";
                                File picDir = new File(picPath);
                                if(!picDir.exists()) {
                                    picDir.mkdirs();
                                }
                                File profile_pic_local = new File(picPath,contact.getUid()+".jpeg");
                                new SaveFile(contact.getProfile_pic(),profile_pic_local).execute();

                                editorPic.putString(contact.getUid(), Uri.fromFile(profile_pic_local).toString());
                                editorPic.apply();
                            }

                        }
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    User contacts = dataSnapshot.getValue(User.class);
                    ListIterator<User> it = userList.listIterator();
                    int tempFlag = 0;
                    while (it.hasNext()) {
                        if (it.next().getPhoneNum().equals(contacts.getPhoneNum())) {
                            int index = it.nextIndex();
                            String tempName = userList.get(index - 1).getName();
                            userList.remove(index - 1);
                            contacts.setName(tempName);
                            contacts.setUid(dataSnapshot.getKey());
                            userList.add(index - 1, contacts);
                            //Log.d("Changed", "Changed " + index);
                            tempFlag = 1;
                        }
                        if (tempFlag == 1) {
                            adapter.notifyDataSetChanged();
                            break;
                        }

                    }
                    //Log.d("Changed Values : ",contacts.getName()+"\n"+contacts.getPhoneNum()+"\n"+contacts.isOnline()+"\n"+contacts.getDevice_token());
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {

                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

    }


    /*

    public void fetch_data() {

        Cursor phones = context.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        final HashMap<String, String> hm = new HashMap<String, String>();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replace(" ", "");
            phoneNumber = phoneNumber.replace("-", "");
            if (phoneNumber.charAt(0) != '+') {
                phoneNumber = "+91" + phoneNumber;
            }
            hm.put(phoneNumber, name);
        }
        phones.close();

        userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
//        userDatabaseReference.keepSynced(true);

        userDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                User user = dataSnapshot.getValue(User.class);
                if(user == null || s == null) return;
                for (Map.Entry kvPair : hm.entrySet()) {
                    if (user.getPhoneNum().equals(kvPair.getKey())) {
                        user.setName(kvPair.getValue().toString());
                        user.setUid(s);
                        Log.d("Lol", "User Added");
                        userList.add(user);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        adapter = new UserAdapter(getContext(), userList, UserAdapter.MODE_SHOW_CONTACTS);
        contactsRecyclerView.setAdapter(adapter);

    }

    */

    @Override
    public void onStart() {
        super.onStart();
        FirebaseDatabase.getInstance().getReference().child("Users").child(currUid).child("isOnline").setValue("true");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_contacts, container, false);

        userList = new ArrayList<>();
        context = container.getContext();

        sharedPreferencesThumb = getContext().getSharedPreferences("thumbInfoLocal",Context.MODE_PRIVATE);
        editorThumb = sharedPreferencesThumb.edit();

        sharedPreferencesPic = getContext().getSharedPreferences("picInfoLocal",Context.MODE_PRIVATE);
        editorPic = sharedPreferencesPic.edit();

        sharedPreferencesThumbLive = getContext().getSharedPreferences("thumbInfoLive",Context.MODE_PRIVATE);
        editorThumbLive = sharedPreferencesThumbLive.edit();

        sharedPreferencesPicLive = getContext().getSharedPreferences("picInfoLive",Context.MODE_PRIVATE);
        editorPicLive = sharedPreferencesPicLive.edit();

        adapter = new ContactsAdapter(rootView.getContext(), userList);


        contactsRecyclerView = rootView.findViewById(R.id.contacts_recyclerView);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        contactsRecyclerView.setHasFixedSize(true);
        MarginDividerItemDecoration itemDecoration = new MarginDividerItemDecoration(getContext());
        contactsRecyclerView.addItemDecoration(itemDecoration);
        contactsRecyclerView.setAdapter(adapter);
        activity = getActivity();

        fetch_data();

        currUid = FirebaseAuth.getInstance().getUid();
        return rootView;

    }

    public class SaveFile extends AsyncTask<Void,Void,Void> {

        String downloadUrl;
        File file;

        public SaveFile(String downloadUrl,File file) {
            this.downloadUrl = downloadUrl;
            this.file = file;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                URL url = new URL(downloadUrl);
                if(file.createNewFile()) {
                    file.createNewFile();
                }

                InputStream is = url.openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG,100,bos);
                byte[] BYTE = bos.toByteArray();
                OutputStream ous = null;
                ous = new FileOutputStream(file);
                ous.write(BYTE);
                ous.close();
                is.close();

            }  catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}