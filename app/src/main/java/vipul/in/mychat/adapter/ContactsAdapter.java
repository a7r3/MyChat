package vipul.in.mychat.adapter;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.SharedPreferenceManager;
import vipul.in.mychat.activity.ChatActivity;
import vipul.in.mychat.activity.ImageDialogActivity;
import vipul.in.mychat.model.User;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private Context mContext;
    private List<User> contactsList;
    private DatabaseReference dbRef;
    private FirebaseUser cUser;

    public ContactsAdapter(Context mContext, List<User> contactsList) {
        this.mContext = mContext;
        this.contactsList = contactsList;
    }

    @Override
    public ContactsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);
        View view = layoutInflater.inflate(R.layout.user, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ContactsViewHolder holder, int position) {

        final User contacts = contactsList.get(position);
        holder.name_from.setText(contacts.getName());
        holder.last_message.setText(contacts.getStatus());

        cUser = FirebaseAuth.getInstance().getCurrentUser();

        if ("true".equals(contacts.getIsOnline())) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }

        SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal",mContext);

        if ("default".equals(contacts.getThumb_pic())) {
            holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            Picasso.get().load(Uri.parse(contacts.getThumb_pic())).into(holder.thumbnail);
        }


        holder.relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, ChatActivity.class);
                intent.putExtra("clicked", holder.name_from.getText().toString());
                intent.putExtra("uid", contacts.getUid());
                SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("thumbInfoLocal",mContext);
                intent.putExtra("friendThumb", sharedPreferenceManager.getData(contacts.getUid()));
                sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal",mContext);
                intent.putExtra("friendProfilePic", sharedPreferenceManager.getData(contacts.getUid()));
                Log.d("Key", "Key: " + contacts.getUid());
                mContext.startActivity(intent);
            }
        });

        holder.thumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent imageDialogIntent = new Intent(mContext, ImageDialogActivity.class);
                SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal",mContext);
                String profile_picture = sharedPreferenceManager.getData(contacts.getUid());
                if ("default".equals(profile_picture)) {
                    imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, ImageDialogActivity.NO_IMAGE_EXTRA);
                } else {
                    imageDialogIntent.putExtra(ImageDialogActivity.IMAGE_URI_EXTRA, profile_picture);
                }
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_NAME_EXTRA, contacts.getName());
                imageDialogIntent.putExtra(ImageDialogActivity.CHAT_UID_EXTRA, contacts.getUid());

                ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) mContext, holder.thumbnail, "image_transition");

                mContext.startActivity(imageDialogIntent, options.toBundle());
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