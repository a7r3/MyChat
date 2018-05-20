package vipul.in.mychat.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import vipul.in.mychat.activity.MainActivity;
import vipul.in.mychat.adapter.ChatListAdapter;
import vipul.in.mychat.model.User;


public class ChatListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    HashMap<String, String> hashMap2;
    DatabaseReference mRef;
    private RecyclerView chatListRecycler;
    private List<User> chatList;
    private ChatListAdapter chatListAdapter;
    private Context context;
    private DatabaseReference chatDatabaseReference;
    private String currUid;

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
        MarginDividerItemDecoration itemDecoration = new MarginDividerItemDecoration(getContext());
        chatListRecycler.addItemDecoration(itemDecoration);
        chatListRecycler.setLayoutManager(linearLayoutManager);
        chatListRecycler.setAdapter(chatListAdapter);

        //fetch_chats();


        FirebaseDatabase.getInstance().getReference().child("Users").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                User newChat = dataSnapshot.getValue(User.class);
                Log.d("MYBIG", newChat.getPhoneNum() + " --number");
                ListIterator<User> listIterator = chatList.listIterator();

                int tempFlag = 0;
                while (listIterator.hasNext()) {

                    if (listIterator.next().getPhoneNum().equals(dataSnapshot.child("phoneNum").getValue(String.class))) {
                        int index = listIterator.nextIndex();
                        Log.d("MYBIG", chatList.get(index - 1).getPhoneNum() + " --compare_number");
                        newChat.setTimestamp(chatList.get(index - 1).getTimestamp());
                        newChat.setLastMessage(chatList.get(index - 1).getLastMessage());
                        newChat.setSeen(chatList.get(index - 1).isSeen());
                        newChat.setUid(chatList.get(index - 1).getUid());
                        newChat.setName(chatList.get(index - 1).getName());
                        chatList.remove(index - 1);
                        chatList.add(index - 1, newChat);
                        tempFlag = 1;
                    }
                    if (tempFlag == 1) {
                        chatListAdapter.notifyDataSetChanged();
                        break;
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

        //yesssss


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

                final User singleChatAdd = dataSnapshot.getValue(User.class);
                Log.d("ChatList", " " + dataSnapshot.getValue().toString());
                final String uid = dataSnapshot.getKey();

//                Log.d("MYLOGS", "log" + uid);

                singleChatAdd.setUid(uid);

                FirebaseDatabase.getInstance().getReference().child("Users").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot ds, String s) {


                        if (ds.getKey().toString().equals(uid)) {
//                            Log.d("MYLOGS", "log" + ds.toString());
                            Iterator iterator = hashMap2.entrySet().iterator();
                            final User tempUser = ds.getValue(User.class);
                            while (iterator.hasNext()) {

                                Map.Entry record = (Map.Entry) iterator.next();
//                                Log.d("MYLOGS", record.getKey().toString());
//                                Log.d("MYLOGS", tempUser.getPhoneNum()/*child("phoneNum").getValue(String.class)*/);
                                if (ds.child("phoneNum").getValue(String.class).equals(record.getKey().toString())) {

                                    singleChatAdd.setThumb_pic(ds.child("thumb_pic").getValue(String.class));
                                    singleChatAdd.setProfile_pic(ds.child("profile_pic").getValue(String.class));
                                    singleChatAdd.setIsOnline(ds.child("isOnline").getValue(String.class));
                                    singleChatAdd.setName(record.getValue().toString());
                                    singleChatAdd.setPhoneNum(record.getKey().toString());
                                    chatList.add(0, singleChatAdd);
                                    chatListAdapter.notifyDataSetChanged();
                                    Log.d("PEHLA","PEHLA");

                                    break;
                                }
                            }
                        }
                    }

                    @Override
                    public void onChildChanged(DataSnapshot ds, String s) {

//                        if (ds.getKey().toString().equals(uid)) {
////                            Log.d("MYLOGS", "log" + ds.toString());
//                            Iterator iterator = hashMap2.entrySet().iterator();
//                            final User tempUser = ds.getValue(User.class);
//                            while (iterator.hasNext()) {
//
//                                Map.Entry record = (Map.Entry) iterator.next();
////                                Log.d("MYLOGS", record.getKey().toString());
////                                Log.d("MYLOGS", tempUser.getPhoneNum()/*child("phoneNum").getValue(String.class)*/);
//                                if (ds.child("phoneNum").getValue(String.class).equals(record.getKey().toString())) {
//
//                                    singleChatAdd.setThumb_pic(ds.child("thumb_pic").getValue(String.class));
//                                    singleChatAdd.setProfile_pic(ds.child("profile_pic").getValue(String.class));
//                                    singleChatAdd.setIsOnline(ds.child("isOnline").getValue(String.class));
//                                    singleChatAdd.setName(record.getValue().toString());
//                                    singleChatAdd.setPhoneNum(record.getKey().toString());
//                                    chatList.add(0, singleChatAdd);
//                                    chatListAdapter.notifyDataSetChanged();
//                                    Log.d("PEHLA","DUSRA");
//                                    break;
//                                }
//                            }
//                        }
                    }

                    @Override
                    public void onChildRemoved(DataSnapshot ds) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot ds, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @Override
            public void onChildChanged(final DataSnapshot dataSnapshot, String s) {

                final User singleChatChange = dataSnapshot.getValue(User.class);
                Log.d("ChatChange", s + " " + dataSnapshot.getValue().toString());
                String uid = dataSnapshot.getKey();
                Log.d("MYNEW" , uid+" Length:"+chatList.size());


                if(chatList.size() == 0) {

                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent);

                } else {
                    reflectChanges(singleChatChange,uid);
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


        //noooooo





        return rootView;
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
        for (Iterator it = list.iterator(); it.hasNext(); ) {
            Map.Entry entry = (Map.Entry) it.next();
            sortedHashMap.put(entry.getKey(), entry.getValue());
        }
        return sortedHashMap;
    }
    public void reflectChanges(User singleChatChange , String uid) {

        singleChatChange.setUid(uid);
        ListIterator<User> iterator = chatList.listIterator();
        int tempFlag = 0;
        while (iterator.hasNext()) {

            String tempUid = iterator.next().getUid();
            //Log.d("MYNEW","tempUid"+tempUid);

            if (tempUid.equals(uid)) {

                Log.d("MYNEW","mainIf");
                int index = iterator.nextIndex();

                Log.d("ChatChange",chatList.get(index-1).toString());
                tempFlag = 1;

                if(chatList.get(index-1).getTimestamp() == singleChatChange.getTimestamp()) {

                    Log.d("MYNEW","pehlaIf");

                    singleChatChange.setName(chatList.get(index-1).getName());
                    singleChatChange.setPhoneNum(chatList.get(index-1).getPhoneNum());
                    singleChatChange.setProfile_pic(chatList.get(index-1).getProfile_pic());
                    singleChatChange.setThumb_pic(chatList.get(index-1).getThumb_pic());
                    singleChatChange.setIsOnline(chatList.get(index-1).getIsOnline());
                    singleChatChange.setUid(chatList.get(index-1).getUid());

                    chatList.remove(index-1);
                    chatList.add(index-1,singleChatChange);

                }
                else {

                    Log.d("MYNEW","pehlaElse");

                    singleChatChange.setName(chatList.get(index-1).getName());
                    singleChatChange.setPhoneNum(chatList.get(index-1).getPhoneNum());
                    singleChatChange.setProfile_pic(chatList.get(index-1).getProfile_pic());
                    singleChatChange.setThumb_pic(chatList.get(index-1).getThumb_pic());
                    singleChatChange.setIsOnline(chatList.get(index-1).getIsOnline());
                    singleChatChange.setUid(chatList.get(index-1).getUid());

                    chatList.remove(index-1);
                    chatList.add(0,singleChatChange);
                }

            }
            else {
                tempFlag = 0 ;
                Log.d("MYNEW","mainElse");
            }

            if(tempFlag == 1) {
                Log.d("MYNEW","notify");
                chatListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

}