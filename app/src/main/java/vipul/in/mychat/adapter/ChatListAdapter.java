package vipul.in.mychat.adapter;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.transition.Explode;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.activity.ImageDialogActivity;
import vipul.in.mychat.model.User;
import vipul.in.mychat.util.Constants;

/**
 * Created by vipul on 23/1/18.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {


    Context mContext;
    List<User> singleChats;
    private Activity activity;
    private OnThumbnailClickListener onThumbnailClickListener;
    private OnItemClickListener onItemClickListener;

    public ChatListAdapter(Context context, List<User> singleChats, Activity activity) {

        this.mContext = context;
        this.singleChats = singleChats;
        this.activity = activity;

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnThumbnailClickListener(OnThumbnailClickListener onThumbnailClickListener) {
        this.onThumbnailClickListener = onThumbnailClickListener;
    }

    @NonNull
    @Override
    public ChatListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.user, parent, false);

        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatListViewHolder holder, int position) {

        final User singleChat = singleChats.get(position);
        holder.name_from.setText(singleChat.getName());
        holder.last_message.setText(singleChat.getLastMessage());

        if (Constants.DEFAULT_PROFILE_PICTURE.equals(singleChat.getThumb_pic())) {
            holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            Picasso.get().load(Uri.parse(singleChat.getThumb_pic())).into(holder.thumbnail);
        }

        if ("true".equals(singleChat.getIsOnline())) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }

    }

    @Override
    public int getItemCount() {
        return singleChats.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.relativeSingleChat)
        RelativeLayout relativeLayout;
        @BindView(R.id.user_name)
        TextView name_from;
        @BindView(R.id.user_msg_or_contact)
        TextView last_message;
        @BindView(R.id.online_indicator)
        ImageView onlineIndicator;
        @BindView(R.id.user_single_image)
        CircleImageView thumbnail;

        public ChatListViewHolder(View itemView) {

            super(itemView);
            ButterKnife.bind(this, itemView);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onItemClickListener != null)
                        onItemClickListener.onItemClicked(v, singleChats.get(getAdapterPosition()));
                }
            });
            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(onThumbnailClickListener != null)
                        onThumbnailClickListener.onThumbnailClicked(v, singleChats.get(getAdapterPosition()));
                }
            });
        }
    }

    public interface OnThumbnailClickListener {
        public void onThumbnailClicked(View v, User selectedUser);
    }

    public interface OnItemClickListener {
        public void onItemClicked(View v, User selectedUser);
    }
}