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
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.SharedPreferenceManager;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.activity.ImageDialogActivity;
import vipul.in.mychat.model.User;

/**
 * Created by vipul on 23/1/18.
 */

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ChatListViewHolder> {


    Context mContext;
    private Activity activity;
    List<User> singleChats;

    public ChatListAdapter(Context context, List<User> singleChats, Activity activity) {

        this.mContext = context;
        this.singleChats = singleChats;
        this.activity = activity;

    }

    @Override
    public ChatListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.user, null);

        return new ChatListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ChatListViewHolder holder, int position) {

        final User singleChat = singleChats.get(position);
        holder.name_from.setText(singleChat.getName());
        holder.last_message.setText(singleChat.getLastMessage());


        SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal", mContext);

        if ("default".equals(singleChat.getThumb_pic())) {
            holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            Picasso.get().load(Uri.parse(singleChat.getThumb_pic())).into(holder.thumbnail);
        }


        if ("true".equals(singleChat.getIsOnline())) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageDialogIntent = new Intent(activity, ImageDialogActivity.class);
                SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal", mContext);
                String profile_picture = sharedPreferenceManager.getData(singleChat.getUid());
                if ("default".equals(profile_picture)) {
                    imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, ImageDialogActivity.NO_IMAGE_EXTRA);
                } else {
                    imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, profile_picture);
                }
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_NAME_EXTRA, singleChat.getName());
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_UID_EXTRA, singleChat.getUid());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.thumbnail, "image_transition");

                activity.startActivity(imageDialogIntent, options.toBundle());
                activity.overridePendingTransition(0, 0);
            }
        });

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final Rect viewRect = new Rect();
                view.getGlobalVisibleRect(viewRect);

                Transition explode = new Explode();
                explode.setEpicenterCallback(new Transition.EpicenterCallback() {
                    @Override
                    public Rect onGetEpicenter(@NonNull Transition transition) {
                        return viewRect;
                    }
                });
                explode.excludeTarget(view, true);
                explode.addListener(new Transition.TransitionListener() {
                    @Override
                    public void onTransitionStart(@NonNull Transition transition) {
                        Log.d("ChatListAdapter", transition.getName() + " start");
                    }

                    @Override
                    public void onTransitionEnd(@NonNull Transition transition) {
                        Log.d("ChatListAdapter", transition.getName() + " end");
                        Intent intent = new Intent(activity, ChatActivity.class);
                        intent.putExtra("clicked", singleChat.getName());
                        intent.putExtra("uid", singleChat.getUid());
                        SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("thumbInfoLocal", mContext);
                        intent.putExtra("friendThumb", sharedPreferenceManager.getData(singleChat.getUid()));
                        sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal", mContext);
                        intent.putExtra("friendProfilePic", sharedPreferenceManager.getData(singleChat.getUid()));
                        activity.startActivity(intent);
                    }

                    @Override
                    public void onTransitionCancel(@NonNull Transition transition) {

                    }

                    @Override
                    public void onTransitionPause(@NonNull Transition transition) {

                    }

                    @Override
                    public void onTransitionResume(@NonNull Transition transition) {

                    }
                });

                explode.setDuration(2000);
                explode.setInterpolator(new AccelerateDecelerateInterpolator());

                TransitionManager.beginDelayedTransition(
                        (ViewGroup) activity.getWindow().getDecorView().getRootView(),
                        explode
                );

            }
        });
    }

    @Override
    public int getItemCount() {
        return singleChats.size();
    }

    public class ChatListViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView name_from, last_message;
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