package vipul.in.mychat.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
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
                            Log.d("Changed", "Changed " + index);
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

        adapter = new ContactsAdapter(rootView.getContext(), userList);


        contactsRecyclerView = rootView.findViewById(R.id.contacts_recyclerView);
        contactsRecyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
        contactsRecyclerView.setHasFixedSize(true);
        MarginDividerItemDecoration itemDecoration = new MarginDividerItemDecoration(getContext());
        contactsRecyclerView.addItemDecoration(itemDecoration);


        contactsRecyclerView.setAdapter(adapter);

        activity = getActivity();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {

            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {

                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Contact permission necessary");
                alertBuilder.setMessage("We need contacts permission to display friends");

                alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 0);
                        AlertDialog alert = alertBuilder.create();
                        alert.show();

                    }
                });
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 0);

            }
        } else {
            fetch_data();
        }
        currUid = FirebaseAuth.getInstance().getUid();
        return rootView;

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 0: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    fetch_data();
                } else {
                }
                return;
            }
        }
    }
}