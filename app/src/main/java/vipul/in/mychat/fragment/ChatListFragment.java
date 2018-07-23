package vipul.in.mychat.fragment;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.transition.Explode;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;

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
import java.util.Objects;
import java.util.Observable;

import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.activity.ImageDialogActivity;
import vipul.in.mychat.util.Constants;
import vipul.in.mychat.util.MarginDividerItemDecoration;
import vipul.in.mychat.R;
import vipul.in.mychat.adapter.ChatListAdapter;
import vipul.in.mychat.model.User;


public class ChatListFragment extends Fragment {

    private final String TAG = getClass().getSimpleName();

    HashMap<String, String> phoneToNameMap;
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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_chat_list, container, false);

        context = container.getContext();
        currUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        chatListRecycler = rootView.findViewById(R.id.chatListRecycler);

        chatList = new ArrayList<>();

        chatListAdapter = new ChatListAdapter(rootView.getContext(), chatList, getActivity());

        chatListAdapter.setOnThumbnailClickListener(new ChatListAdapter.OnThumbnailClickListener() {
            @Override
            public void onThumbnailClicked(View v, User singleChat) {
                Intent imageDialogIntent = new Intent(getActivity(), ImageDialogActivity.class);
                imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, singleChat.getProfile_pic());
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_NAME_EXTRA, singleChat.getName());
                imageDialogIntent.putExtra(Constants.RECEIVER_UID_EXTRA, singleChat.getUid());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        Objects.requireNonNull(getActivity()), v, "image_transition");

                startActivity(imageDialogIntent, options.toBundle());
                getActivity().overridePendingTransition(0, 0);
            }
        });

        chatListAdapter.setOnItemClickListener(new ChatListAdapter.OnItemClickListener() {
            @Override
            public void onItemClicked(View view, User singleChat) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra(Constants.SELECTED_USER_NAME_EXTRA, singleChat.getName());
                intent.putExtra(Constants.RECEIVER_UID_EXTRA, singleChat.getUid());
                intent.putExtra(Constants.SPREF_FRIEND_THUMB, singleChat.getThumb_pic());
                intent.putExtra("friendProfilePic", singleChat.getProfile_pic());
                startActivity(intent);
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(rootView.getContext());
        MarginDividerItemDecoration itemDecoration = new MarginDividerItemDecoration(getContext());
        chatListRecycler.addItemDecoration(itemDecoration);
        chatListRecycler.setLayoutManager(linearLayoutManager);
        chatListRecycler.setAdapter(chatListAdapter);


        Cursor phones = getContext().getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

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

        mRef = FirebaseDatabase.getInstance().getReference();
        mRef.keepSynced(true);

        mRef.child(Constants.FIREBASE_CHATS_NODE).child(currUid).orderByChild(Constants.FIREBASE_CHATS_TIMESTAMP).addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, String s) {

                final User singleChatAdd = dataSnapshot.getValue(User.class);
                final String uid = dataSnapshot.getKey();

                singleChatAdd.setUid(uid);

                mRef.child(Constants.FIREBASE_USERS_NODE).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot ds) {
                        String phoneNum = ds.child(Constants.FIREBASE_USER_PHONE_NUM).getValue(String.class);
                        if (phoneToNameMap.containsKey(phoneNum)) {
                            singleChatAdd.setThumb_pic(ds.child(Constants.FIREBASE_USER_THUMB_PIC).getValue(String.class));
                            singleChatAdd.setProfile_pic(ds.child(Constants.FIREBASE_USER_PROFILE_PIC).getValue(String.class));
                            singleChatAdd.setIsOnline(ds.child(Constants.FIREBASE_USER_IS_ONLINE).getValue(String.class));
                            singleChatAdd.setName(phoneToNameMap.get(phoneNum));
                            singleChatAdd.setPhoneNum(phoneNum);
                            chatList.add(0, singleChatAdd);
                            chatListAdapter.notifyDataSetChanged();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onChildChanged(final @NonNull DataSnapshot dataSnapshot, String s) {

                final User singleChatChange = dataSnapshot.getValue(User.class);
                Log.d(TAG, "ChatChange" + s + " " + dataSnapshot.getValue().toString());
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
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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

                Log.d(TAG, "ChatChange" + chatList.get(index - 1).toString());
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