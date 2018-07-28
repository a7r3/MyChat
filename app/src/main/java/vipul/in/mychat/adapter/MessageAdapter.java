package vipul.in.mychat.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.model.Message;
import vipul.in.mychat.util.Constants;
import vipul.in.mychat.util.UtilityMethods;

/**
 * Created by vipul on 22/1/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private final String TAG = getClass().getSimpleName();
    public boolean isSendButtonUsed = false;
    private Context context;
    private List<Message> messageList;
    private String myThumb, friendThumb;
    private String latestDate = null;
    private RecyclerView recyclerView;
    private Calendar calendar = Calendar.getInstance();
    private SimpleDateFormat timeFormatPattern = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private ConstraintSet set = new ConstraintSet();

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
        // From this time in milliseconds
        calendar.setTimeInMillis(message.getTime());

        // Use the formatter to get the Date, with the format specified earlier
        String date = dateFormat.format(calendar.getTime());

        if (isSendButtonUsed) {
            if (messageList.get(getItemCount() - 1).getFrom().equals(message.getFrom())) {
                ((MessageViewHolder) recyclerView
                        .findViewHolderForAdapterPosition(getItemCount() - 1))
                        .profileImage
                        .setVisibility(View.INVISIBLE);
            }
        }

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

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(viewType, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if (messageList.get(position).getFrom().equals("Me"))
            return R.layout.outgoing_message_layout;
        else
            return R.layout.incoming_message_layout;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        holder.bindViewHolder(messageList.get(holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    class MessageViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.message_bubble)
        ConstraintLayout messageBubble;
        @BindView(R.id.time_text_layout)
        TextView messageTime;
        @BindView(R.id.message_text_layout)
        TextView messageView;
        @BindView(R.id.message_profile_picture)
        CircleImageView profileImage;
        @BindView(R.id.start_of_day_view)
        View dateView;
        @BindView(R.id.image_send_layout)
        ImageView imageView;
        @BindView(R.id.date_text)
        TextView dateText;

        MessageViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }

        public void bindViewHolder(final Message message) {

            // Show Profile Picture only when the next message is of the other user
            String currentMessageUser = message.getFrom();
            ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) messageBubble.getLayoutParams();
            if (getAdapterPosition() != getItemCount() - 1) {
                String nextMessageUser = messageList.get(getAdapterPosition() + 1).getFrom();
                params.bottomMargin = (int) UtilityMethods.convertDpToPixel(4.0f, context);
                if (!currentMessageUser.equals(nextMessageUser)) {
                    params.bottomMargin = (int) UtilityMethods.convertDpToPixel(8.0f, context);
                    messageBubble.setLayoutParams(params);
                    profileImage.setVisibility(View.VISIBLE);
                } else if (messageDates.contains(getAdapterPosition() + 1)) {
                    profileImage.setVisibility(View.VISIBLE);
                } else {
                    profileImage.setVisibility(View.INVISIBLE);
                }
            } else {
                profileImage.setVisibility(View.VISIBLE);
            }
            messageBubble.setLayoutParams(params);

            String from_user = message.getFrom();
            String messageType = message.getType();

            calendar.setTimeInMillis(message.getTime());

            // If this message's position is present in the Wanted List
            // Then make the 'Date View' visible
            if (messageDates.contains(getAdapterPosition())) {
                dateView.setVisibility(View.VISIBLE);
                String date = dateFormat.format(calendar.getTime());
                dateText.setText(date);
            } else {
                dateView.setVisibility(View.GONE);
            }

            // Loading Profile Picture
            Picasso.get()
                    .load((!from_user.equals("Me")) ? Uri.parse(friendThumb) : Uri.parse(myThumb))
                    .error(R.drawable.ic_person_black_24dp)
                    .placeholder(R.drawable.ic_person_black_24dp)
                    .networkPolicy(NetworkPolicy.OFFLINE)
                    .into(profileImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError(Exception e) {
//                            Picasso.get()
//                                    .load((!from_user.equals("Me")) ? Uri.parse(friendThumb) : Uri.parse(myThumb))
//                                    .error(R.drawable.ic_person_black_24dp)
//                                    .placeholder(R.drawable.ic_person_black_24dp)
//                                    .into(profileImage);
                        }
                    });

            final String time = timeFormatPattern.format(calendar.getTime());
            messageTime.setText(time);

            // TODO: Support other media types
            if (messageType.equals(Constants.MESSAGE_TYPE_TEXT)) {
                messageView.setVisibility(View.VISIBLE);
                imageView.setVisibility(View.GONE);
                messageView.setText(message.getMessage().trim());
                messageView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                    @Override
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        // Doing it for once
                        messageView.removeOnLayoutChangeListener(this);

                        final int messageViewWidth = Math.round(right - left);

                        messageTime.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                            @Override
                            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                                // Doing it for once
                                messageTime.removeOnLayoutChangeListener(this);

                                // If either of the Time/Message views have negative widths, abort
                                if (messageViewWidth <= 0) return;
                                float messageTimeWidth = Math.abs(right - left);
                                if (messageTimeWidth <= 0) return;

                                // The Space occupied by the last line of the message
                                float offset = messageView.getLayout().getLineWidth(messageView.getLineCount() - 1);
                                // Calculating max width. The message bubble should stay under this width
                                float maxWidth = itemView.getMeasuredWidth() - UtilityMethods.convertDpToPixel(48.0f + 36.0f + 8.0f + 8.0f, context);

                                Log.d(TAG, "Position " + getAdapterPosition()
                                        + " | maxWidth " + maxWidth
                                        + " | width " + (messageTimeWidth + messageViewWidth)
                                        + " | messageTimeWidth " + messageTimeWidth
                                        + " | messageViewWidth " + messageViewWidth
                                        + " | offset " + offset
                                        + " | offcalc " + (messageViewWidth - offset)
                                        + " | Message : " + message.getMessage());

                                set.clone(messageBubble);

                                // If the Time TextView is small enough to sit after the space after the last line of Message
                                if (messageTimeWidth < (messageViewWidth - offset)) {
                                    // Then we place it along the message
                                    // Log.d(TAG, "Placing along");
                                    set.clear(R.id.time_text_layout, ConstraintSet.TOP);
                                    set.clear(R.id.time_text_layout, ConstraintSet.START);
                                } else if (messageTimeWidth + messageViewWidth < maxWidth) {
                                    // In this case, if the width of Time and Message Views together is lesser than the Maximum width
                                    // Then we place the time View next to the Message View
                                    //  Log.d(TAG, "Placing Adjacent");
                                    set.connect(R.id.time_text_layout, ConstraintSet.START, R.id.message_text_layout, ConstraintSet.END);
                                    set.clear(R.id.time_text_layout, ConstraintSet.TOP);
                                } else {
                                    // Log.d(TAG, "Placing below");
                                    // In this case, the message can neither be placed along, or adjacent to the message view
                                    // So we place the Time TextView below the Message View
                                    set.connect(R.id.time_text_layout, ConstraintSet.TOP, R.id.message_text_layout, ConstraintSet.BOTTOM);
                                    set.clear(R.id.time_text_layout, ConstraintSet.START);
                                }

                                set.applyTo(messageBubble);
//                                ConstraintLayout.LayoutParams p = (ConstraintLayout.LayoutParams) messageTime.getLayoutParams();
//                                Log.d(TAG, "startToEnd : " + context.getResources().getResourceEntryName((p.startToEnd == -1) ? R.id.on : p.startToEnd) + " topToBottom : "
//                                        + context.getResources().getResourceEntryName((p.topToBottom == -1) ? R.id.on : p.topToBottom));
                            }
                        });
                    }
                });
            } else if (messageType.equals(Constants.MESSAGE_TYPE_IMAGE)) {
                messageView.setText("");
                messageView.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                set.clone(messageBubble);
                // For an image, we'd place the Time TextView along with the image, irrespective of the image size
                Log.d(TAG, "Placing along");
                set.clear(R.id.time_text_layout, ConstraintSet.TOP);
                set.clear(R.id.time_text_layout, ConstraintSet.START);
                set.applyTo(messageBubble);
                Picasso.get()
                        .load(Uri.parse(message.getMessage()))
                        .networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.ic_person_black_24dp)
                        .into(imageView, new Callback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onError(Exception e) {
//                                Picasso.get()
//                                        .load(Uri.parse(message.getMessage()))
//                                        .placeholder(R.drawable.ic_person_black_24dp)
//                                        .into(holder.imageView);
                            }
                        });
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // TODO: Show Message Options
                    // 1. Copy
                    // 2. Forward
                    // 3. Star it
                }
            });

        }

    }
}
