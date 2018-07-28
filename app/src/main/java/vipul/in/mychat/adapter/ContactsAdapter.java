package vipul.in.mychat.adapter;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import vipul.in.mychat.R;
import vipul.in.mychat.model.User;
import vipul.in.mychat.util.Constants;

public class ContactsAdapter extends RecyclerView.Adapter<ContactsAdapter.ContactsViewHolder> {

    private Context context;
    private List<User> contactsList;
    private Activity activity;
    private OnItemClickListener onItemClickListener;
    private OnThumbnailClickListener onThumbnailClickListener;

    public ContactsAdapter(Activity activity, Context context, List<User> contactsList) {
        this.activity = activity;
        this.context = context;
        this.contactsList = contactsList;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnThumbnailClickListener(OnThumbnailClickListener onThumbnailClickListener) {
        this.onThumbnailClickListener = onThumbnailClickListener;
    }

    @NonNull
    @Override
    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(R.layout.user, parent, false);
        return new ContactsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position) {

        final User contacts = contactsList.get(position);
        holder.name_from.setText(contacts.getName());
        holder.last_message.setText(contacts.getStatus());

        if ("true".equals(contacts.getIsOnline())) {
            holder.onlineIndicator.setImageResource(R.drawable.online);
        } else {
            holder.onlineIndicator.setImageResource(R.drawable.offline);
        }


        if (Constants.DEFAULT_PROFILE_PICTURE.equals(contacts.getThumb_pic())) {
            holder.thumbnail.setImageResource(R.drawable.ic_person_black_24dp);
        } else {
            Picasso.get().load(Uri.parse(contacts.getThumb_pic())).networkPolicy(NetworkPolicy.OFFLINE).into(holder.thumbnail, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError(Exception e) {
                    Picasso.get().load(Uri.parse(contacts.getThumb_pic())).into(holder.thumbnail);
                }
            });
        }

    }


    @Override
    public int getItemCount() {
        return contactsList.size();
    }

    public interface OnThumbnailClickListener {
        public void onThumbnailClicked(View v, User selectedUser);
    }

    public interface OnItemClickListener {
        public void onItemClicked(View v, User selectedUser);
    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {

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

        public ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onItemClickListener != null)
                        onItemClickListener.onItemClicked(v, contactsList.get(getAdapterPosition()));
                }
            });

            thumbnail.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (onThumbnailClickListener != null)
                        onThumbnailClickListener.onThumbnailClicked(v, contactsList.get(getAdapterPosition()));
                }
            });
        }
    }
}