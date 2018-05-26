package vipul.in.mychat.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;
import hani.momanii.supernova_emoji_library.Helper.EmojiconTextView;
import vipul.in.mychat.R;
import vipul.in.mychat.model.Message;

/**
 * Created by vipul on 22/1/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final int OUTGOING_MESSAGE_VIEW = 0;
    private final int INCOMING_MESSAGE_VIEW = 1;
    private Context context;
    private List<Message> messageList;
    private String myThumb, friendThumb;
    private String latestDate = null;
    // Contains a list of positions of message
    // These messages indicate the start of a day
    // At these positions, the 'Date View' is made visible
    private List<Integer> messageDates = new ArrayList<>();

    public MessageAdapter(List<Message> messageList, Context context, String myThumb, String friendThumb) {
        this.messageList = messageList;
        this.context = context;
        this.myThumb = myThumb;
        this.friendThumb = friendThumb;
    }

    /**
     * Function to add an message to the Message List
     * And check if this message is the first message of the Day
     * <p>
     * If so, record this message's position in a List
     * And Show the 'Date View' for that position
     * <p>
     * 'Date View' shows the date, indicates that the messages below it
     * were sent on that Date -- till the next 'Date View'
     *
     * @param message The message to be added
     */
    public void addMessage(Message message) {
        // Get the date in the specified format, this is the formatter
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        Calendar calendar = Calendar.getInstance();
        // From this time in milliseconds
        calendar.setTimeInMillis(message.getTime());

        // Use the formatter to get the Date, with the format specified earlier
        String date = dateFormat.format(calendar.getTime());

        // Add the message as usual
        messageList.add(message);
        notifyItemInserted(messageList.size() - 1);

        // If this is the first message, the Date View should appear obviously
        if (latestDate == null) {
            messageDates.add(messageList.size() - 1);
            latestDate = date;
        }

        // If current date is different than the previous date
        // Then mark this position for showing the Date View
        // Showing the Date View indicates the start of conversation on that particular day
        if (!date.equals(latestDate)) {
            messageDates.add(messageList.size() - 1);
            latestDate = date;
        }

    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutResId = R.layout.incoming_message_layout;
        if (viewType == OUTGOING_MESSAGE_VIEW)
            layoutResId = R.layout.outgoing_message_layout;
        View view = LayoutInflater.from(context).inflate(layoutResId, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getFrom().equals("Me"))
            return OUTGOING_MESSAGE_VIEW;
        else
            return INCOMING_MESSAGE_VIEW;
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        super.setHasStableIds(true);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {

        Message message = messageList.get(position);
        String from_user = message.getFrom();
        String message_type = message.getType();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(message.getTime());

        // If this message's position is present in the Wanted List
        // Then make the 'Date View' visible
        if (messageDates.contains(position)) {
            holder.dateView.setVisibility(View.VISIBLE);
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
            String date = dateFormat.format(calendar.getTime());
            holder.dateText.setText(date);
        } else {
            holder.dateView.setVisibility(View.GONE);
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String time = dateFormat.format(calendar.getTime());
        holder.messageTime.setText(time);

        holder.displayName.setText(from_user);

        if (from_user.equals("Me")) {
            if ("default".equals(myThumb)) {
                holder.profileImage.setImageResource(R.drawable.ic_person_black_24dp);
            } else {
                Picasso.get().load(Uri.parse(myThumb)).into(holder.profileImage);
            }
        } else {
            if ("default".equals(friendThumb)) {
                holder.profileImage.setImageResource(R.drawable.ic_person_black_24dp);
            } else {
                holder.profileImage.setImageURI(Uri.parse(friendThumb));
            }
        }

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
        private CircleImageView profileImage;
        private View dateView;
        private TextView dateText;

        MessageViewHolder(View v) {
            super(v);
            messageView = v.findViewById(R.id.message_text_layout);
            profileImage = v.findViewById(R.id.message_profile_picture);
            displayName = v.findViewById(R.id.name_text_layout);
            messageTime = v.findViewById(R.id.time_text_layout);
            dateView = v.findViewById(R.id.start_of_day_view);
            dateText = dateView.findViewById(R.id.date_text);
        }
    }
}
