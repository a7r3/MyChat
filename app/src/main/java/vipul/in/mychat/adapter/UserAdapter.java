package vipul.in.mychat.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import vipul.in.mychat.R;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.model.User;
import vipul.in.mychat.util.Constants;

/**
 * Created by vipul on 23/1/18.
 */

// Until the current adapters are combined back into this Adapter, this is ...
@Deprecated
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {

    public static final int MODE_SHOW_CHATS = 220;
    public static final int MODE_SHOW_CONTACTS = 420;
    private Context context;
    private List<User> users;
    private int mode;

    public UserAdapter(Context context, List<User> users, int mode) {
        this.users = users;
        this.context = context;
        this.mode = mode;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final UserViewHolder holder, int position) {

        User user = users.get(holder.getAdapterPosition());

        holder.userNameText.setText(user.getName());

        switch (mode) {
            case MODE_SHOW_CHATS:
                holder.lastMessageOrNumberText.setText(user.getLastMessage());
            case MODE_SHOW_CONTACTS:
                holder.lastMessageOrNumberText.setText(user.getPhoneNum());
            default:
        }

        if (user.getIsOnline().equals("true")) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }

    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView userNameText;
        TextView lastMessageOrNumberText;
        ImageView onlineIndicator;

        UserViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relativeSingleChat);
            userNameText = itemView.findViewById(R.id.user_name);
            lastMessageOrNumberText = itemView.findViewById(R.id.user_msg_or_contact);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, ChatActivity.class);
                    intent.putExtra(Constants.SELECTED_USER_NAME_EXTRA, users.get(getAdapterPosition()).getName());
                    intent.putExtra(Constants.RECEIVER_UID_EXTRA, users.get(getAdapterPosition()).getUid());
                    context.startActivity(intent);
                }
            });
        }
    }
}
