package vipul.in.mychat.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
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

import vipul.in.mychat.R;
import vipul.in.mychat.adapter.ChatListAdapter;
import vipul.in.mychat.adapter.UserAdapter;
import vipul.in.mychat.model.User;


public class ChatListFragment extends Fragment {

    private RecyclerView chatListRecycler;
    private List<User> chatList;
    private ChatListAdapter chatListAdapter;
    private Context context;
    private DatabaseReference chatDatabaseReference;
    private String currUid;
    private final String TAG = getClass().getSimpleName();

    HashMap<String,String> hashMap2;
    DatabaseReference mRef;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);

        context = container.getContext();
        currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        chatListRecycler = rootView.findViewById(R.id.chatListRecycler);
        chatList = new ArrayList<>();
        chatListAdapter = new ChatListAdapter(rootView.getContext(), chatList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
//        linearLayoutManager.setReverseLayout(true);
//        linearLayoutManager.setStackFromEnd(true);
        chatListRecycler.addItemDecoration(new DividerItemDecoration(context, DividerItemDecoration.VERTICAL));
        chatListRecycler.setLayoutManager(linearLayoutManager);
        chatListRecycler.setAdapter(chatListAdapter);


        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS)) {
                final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                alertBuilder.setCancelable(true);
                alertBuilder.setTitle("Permission Required");
                alertBuilder.setMessage("MyChat requires the 'Contacts' permission to get connected with your Contacts");

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
            fetch_chats();
        }

        return rootView;
    }

    /*

    private void fetch_chats() {

        chatDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Chats").child(currUid);

        final DatabaseReference userDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        chatListAdapter = new ChatListAdapter(getContext(), chatList);
        chatListRecycler.setAdapter(chatListAdapter);

        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        final HashMap<String, String> phoneToUserMap = new HashMap<String, String>();
        while (phones.moveToNext()) {
            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
            phoneNumber = phoneNumber.replace(" ", "");
            phoneNumber = phoneNumber.replace("-", "");
            if (phoneNumber.charAt(0) != '+') {
                phoneNumber = "+91" + phoneNumber;
            }
            phoneToUserMap.put(phoneNumber, name);
        }
        phones.close();

        chatDatabaseReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                final User user = dataSnapshot.getValue(User.class);
                if(user == null || s == null) return;
                user.setUid(s);
                userDatabaseReference.child(s).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        User finalUser = dataSnapshot.getValue(User.class);
                        if(finalUser == null) return;
                        finalUser.setName(phoneToUserMap.get(finalUser.getPhoneNum()));
                        finalUser.setUid(user.getUid());
                        finalUser.setLastMessage(user.getLastMessage());
                        finalUser.setSeen(user.isSeen());
                        finalUser.setTimestamp(user.getTimestamp());
                        chatList.add(finalUser);
                        chatListAdapter.notifyItemInserted(chatList.size() - 1);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                final User user = dataSnapshot.getValue(User.class);
                if(user == null && s == null) return;
                user.setUid(s);
                for (int i = 0; i < chatList.size(); i++) {
                    if (chatList.get(i).getUid().equals(s)) {
                        user.setName(phoneToUserMap.get(user.getPhoneNum()));
                        // Set the UID of the user = Name of the Node
                        chatList.remove(i);
                        chatListAdapter.notifyItemRemoved(i);
                        Log.d(TAG, "User " + user.getUid() + " Replaced");
                        chatList.add(user);
                        chatListAdapter.notifyItemInserted(chatList.size() - 1);
                    }
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                if(user != null) {
                    for (int i = 0; i < chatList.size(); i++) {
                        if (chatList.get(i).getPhoneNum().equals(user.getPhoneNum())) {
                            Log.d(TAG, "User " + chatList.get(i).getUid() + " Removed");
                            chatList.remove(i);
                            chatListAdapter.notifyItemRemoved(i);
                        }
                    }
                }
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    */

    private void fetch_chats() {


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

        hashMap2 = sortByValues(hm);
        Log.d("All contacts: ", hashMap2.toString());
        phones.close();


        mRef = FirebaseDatabase.getInstance().getReference();

        mRef.child("Chats").child(currUid).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final User singleChat = dataSnapshot.getValue(User.class);
                Log.d("ChatList"," "+dataSnapshot.getValue().toString());
                String uid = dataSnapshot.getKey();
                singleChat.setUid(uid);

                mRef.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot ds) {

                        Iterator iterator = hashMap2.entrySet().iterator();
                        while(iterator.hasNext()) {

                            Map.Entry record = (Map.Entry) iterator.next();
                            if (ds.child("phoneNum").getValue(String.class).equals(record.getKey().toString())) {

                                singleChat.setThumb_pic(ds.child("thumb_pic").getValue(String.class));
                                singleChat.setProfile_pic(ds.child("profile_pic").getValue(String.class));
                                singleChat.setName(record.getValue().toString());
                                chatList.add(0,singleChat);
                                chatListAdapter.notifyDataSetChanged();
                                break;

                            }

                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {

                final User singleChat = dataSnapshot.getValue(User.class);
                Log.d("ChatChange",s+" "+dataSnapshot.getValue().toString());
                String uid = dataSnapshot.getKey();
                singleChat.setUid(uid);

                ListIterator i = chatList.listIterator();
                while (i.hasNext()) {

                    User sc = (User) i.next();
                    if(sc.getUid().equals(uid)) {

                        if(sc.getTimestamp() == singleChat.getTimestamp()) {

                            singleChat.setName(sc.getName());
                            chatList.remove(i.nextIndex()-1);
                            chatList.add(i.nextIndex()-1,singleChat);
                            chatListAdapter.notifyDataSetChanged();
                            break;

                        }
                        else {

                            singleChat.setName(sc.getName());
                            chatList.remove(i.nextIndex()-1);
                            chatList.add(0,singleChat);
                            chatListAdapter.notifyDataSetChanged();
                            break;

                        }


                    }

                }


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

    private HashMap sortByValues(HashMap map) {
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
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }

}