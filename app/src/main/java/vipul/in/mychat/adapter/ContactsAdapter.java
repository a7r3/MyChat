package vipul.in.mychat.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.activity.ImageDialogActivity;
import vipul.in.mychat.model.User;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private Context context;
    private List<User> contactsList;
    private Activity activity;

    public ContactsAdapter(Activity activity, Context context, List<User> contactsList) {
        this.activity = activity;
        this.context = context;
        this.contactsList = contactsList;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.user, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactsViewHolder holder, int position) {

        final User contacts = contactsList.get(position);
        holder.name_from.setText(contacts.getName());
        holder.last_message.setText(contacts.getStatus());

        if ("true".equals(contacts.getIsOnline())) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }


        if ("default".equals(contacts.getThumb_pic())) {
            holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            Picasso.with(context).load(Uri.parse(contacts.getThumb_pic())).networkPolicy(NetworkPolicy.OFFLINE).into(holder.thumbnail, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {
                    Picasso.with(context).load(Uri.parse(contacts.getThumb_pic())).into(holder.thumbnail);
                }
            });
        }

        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(activity, ChatActivity.class);
                intent.putExtra("clicked", holder.name_from.getText().toString());
                intent.putExtra("uid", contacts.getUid());
                intent.putExtra("friendThumb", contacts.getThumb_pic());
                intent.putExtra("friendProfilePic", contacts.getProfile_pic());
                Log.d("Key", "Key: " + contacts.getUid());
                activity.startActivity(intent);
            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageDialogIntent = new Intent(activity, ImageDialogActivity.class);
                if ("default".equals(contacts.getProfile_pic())) {
                    imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, ImageDialogActivity.NO_IMAGE_EXTRA);
                } else {
                    imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, contacts.getProfile_pic());
                }
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_NAME_EXTRA, contacts.getName());
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_UID_EXTRA, contacts.getUid());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity, holder.thumbnail, "image_transition");

                activity.startActivity(imageDialogIntent, options.toBundle());
                activity.overridePendingTransition(0, 0);
            }
        });

    }


    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView name_from, last_message;
        ImageView onlineIndicator;
        CircleImageView thumbnail;

        public ContactsViewHolder(View itemView) {
            super(itemView);
            relativeLayout = itemView.findViewById(R.id.relativeSingleChat);
            name_from = itemView.findViewById(R.id.user_name);
            last_message = itemView.findViewById(R.id.user_msg_or_contact);
            onlineIndicator = itemView.findViewById(R.id.online_indicator);
            thumbnail = itemView.findViewById(R.id.user_single_image);
        }
    }
}