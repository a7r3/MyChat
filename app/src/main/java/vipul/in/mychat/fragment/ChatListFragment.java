package vipul.in.mychat.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import vipul.in.mychat.adapter.ChatListAdapter;
import vipul.in.mychat.model.User;


public class ChatListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    HashMap phoneToNameMap;
    DatabaseReference mRef;
    private RecyclerView chatListRecycler;
    private List<User> chatList;
    private ChatListAdapter chatListAdapter;
    private Context context;
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

        chatListAdapter = new ChatListAdapter(rootView.getContext(), chatList, getActivity());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        MarginDividerItemDecoration itemDecoration = new MarginDividerItemDecoration(getContext());
        chatListRecycler.addItemDecoration(itemDecoration);
        chatListRecycler.setLayoutManager(linearLayoutManager);
        chatListRecycler.setAdapter(chatListAdapter);


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

        phoneToNameMap = sortByValues(hm);
        phones.close();

        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.keepSynced(true);

        mRef.child("Chats").child(currUid).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                final User singleChatAdd = dataSnapshot.getValue(User.class);
                final String uid = dataSnapshot.getKey();

                singleChatAdd.setUid(uid);

                mRef.child("Users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot ds) {
                        String phoneNum = ds.child("phoneNum").getValue(String.class);
                        if (phoneToNameMap.containsKey(phoneNum)) {
                            singleChatAdd.setThumb_pic(ds.child("thumb_pic").getValue(String.class));
                            singleChatAdd.setProfile_pic(ds.child("profile_pic").getValue(String.class));
                            singleChatAdd.setIsOnline(ds.child("isOnline").getValue(String.class));
                            singleChatAdd.setName(phoneToNameMap.get(phoneNum).toString());
                            singleChatAdd.setPhoneNum(phoneNum);
                            chatList.add(0, singleChatAdd);
                            chatListAdapter.notifyDataSetChanged();
                        }
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

                if (chatList.size() == 0) {

                    Intent intent = getActivity().getIntent();
                    getActivity().finish();
                    startActivity(intent);

                } else {
                    reflectChanges(singleChatChange, uid);
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

    public void reflectChanges(User singleChatChange, String uid) {

        singleChatChange.setUid(uid);
        ListIterator<User> iterator = chatList.listIterator();
        int tempFlag = 0;
        while (iterator.hasNext()) {

            String tempUid = iterator.next().getUid();
            if (tempUid.equals(uid)) {
                int index = iterator.nextIndex();

                Log.d("ChatChange", chatList.get(index - 1).toString());
                tempFlag = 1;

                if (chatList.get(index - 1).getTimestamp() == singleChatChange.getTimestamp()) {
                    singleChatChange.setName(chatList.get(index - 1).getName());
                    singleChatChange.setPhoneNum(chatList.get(index - 1).getPhoneNum());
                    singleChatChange.setProfile_pic(chatList.get(index - 1).getProfile_pic());
                    singleChatChange.setThumb_pic(chatList.get(index - 1).getThumb_pic());
                    singleChatChange.setIsOnline(chatList.get(index - 1).getIsOnline());
                    singleChatChange.setUid(chatList.get(index - 1).getUid());
                    chatList.remove(index - 1);
                    chatList.add(index - 1, singleChatChange);
                    chatListAdapter.notifyItemChanged(index - 1);
                } else {
                    singleChatChange.setName(chatList.get(index - 1).getName());
                    singleChatChange.setPhoneNum(chatList.get(index - 1).getPhoneNum());
                    singleChatChange.setProfile_pic(chatList.get(index - 1).getProfile_pic());
                    singleChatChange.setThumb_pic(chatList.get(index - 1).getThumb_pic());
                    singleChatChange.setIsOnline(chatList.get(index - 1).getIsOnline());
                    singleChatChange.setUid(chatList.get(index - 1).getUid());
                    chatList.remove(index - 1);
                    chatList.add(0, singleChatChange);
                    chatListAdapter.notifyItemMoved(index - 1, 0);
                }

            } else {
                tempFlag = 0;
            }

            if (tempFlag == 1) {
                chatListAdapter.notifyDataSetChanged();
                break;
            }
        }
    }

}