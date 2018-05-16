package vipul.in.mychat.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import vipul.in.mychat.R;
import vipul.in.mychat.Utils;
import vipul.in.mychat.model.Message;

/**
 * Created by vipul on 22/1/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private Context context;
    private List<Message> messageList;
    private final int OUTGOING_MESSAGE_VIEW = 0;
    private final int INCOMING_MESSAGE_VIEW = 1;
    private final int DATE_VIEW = 2;

    public MessageAdapter(List<Message> mMessageList, Context context) {
        this.messageList = mMessageList;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId = R.layout.incoming_message_layout;
        if(viewType == OUTGOING_MESSAGE_VIEW)
            layoutResId = R.layout.outgoing_message_layout;
        else if(viewType == DATE_VIEW)
            layoutResId = R.layout.date_layout;
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        // TODO: Show Date between messages
        // DATE_VIEW

        if(messageList.get(position).getFrom().equals("Me"))
            return OUTGOING_MESSAGE_VIEW;
        else
            return INCOMING_MESSAGE_VIEW;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        Log.d("LOLL", "onBind " + messageList.get(position).getMessage() );
        Message message = messageList.get(position);
        String from_user = message.getFrom();
        String message_type = message.getType();

        holder.messageTime.setText(Utils.getTimeAgo(message.getTime()));

        holder.displayName.setText(from_user);

        // TODO: Support other media types
        if (message_type.equals("text")) {
            holder.messageView.setText(message.getMessage());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Show Message Options
                // 1. Copy
                // 2. Forward
                // 3. Star it
            }
        });
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {

        private TextView displayName, messageTime;
        private EmojiconTextView messageView;
        private ImageView profileImage;

        MessageViewHolder(View v) {
            super(v);
            messageView = v.findViewById(R.id.message_text_layout);
            profileImage = v.findViewById(R.id.message_profile_layout);
            displayName = v.findViewById(R.id.name_text_layout);
            messageTime = v.findViewById(R.id.time_text_layout);
        }

    }
}
