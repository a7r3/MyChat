package vipul.in.mychat.adapter;


import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.model.User;

/**
 * Created by vipul on 23/1/18.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {


    Context mContext;
    List<User> singleChats;

    public ChatListAdapter(Context context, List<User> singleChats) {

        this.mContext = context;
        this.singleChats = singleChats;

    }

    @Override
    public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.user,null);

        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ChatListViewHolder holder, int position) {

        final User singleChat = singleChats.get(position);
        holder.name_from.setText(singleChat.getName());
        holder.last_message.setText(singleChat.getLastMessage());

        if("default".equals(singleChat.getThumb_pic())) {
            holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        }
        else {
            Log.d("DEBUG",singleChat.getName());
            Picasso.get().load(Uri.parse(singleChat.getThumb_pic())).into(holder.thumbnail);
        }

        if("true".equals(singleChat.getIsOnline())) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("clicked",singleChat.getName());
                intent.putExtra("uid",singleChat.getUid());
                intent.putExtra("friendThumb",singleChat.getThumb_pic());
                intent.putExtra("friendProfilePic",singleChat.getProfile_pic());
                mContext.startActivity(intent);

            }
        });
    }

    @Override
    public int getItemCount() {
        return singleChats.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder{

        RelativeLayout relativeLayout;
        TextView name_from,last_message;
        ImageView onlineIndicator;
        CircleImageView thumbnail;
        public ChatListViewHolder(View itemView) {

            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relativeSingleChat);
            name_from = itemView.findViewById(R.id.user_name);
            thumbnail = itemView.findViewById(R.id.user_single_image);
            last_message = itemView.findViewById(R.id.user_msg_or_contact);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
        }
    }
}