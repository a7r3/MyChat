package vipul.in.mychat.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.transition.TransitionManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import vipul.in.mychat.R;
import vipul.in.mychat.SharedPreferenceManager;

public class ImageDialogActivity extends Activity {

    public static final String CHAT_NAME_EXTRA = "chat_name";
    public static final String IMAGE_URI_EXTRA = "chat_profile_image_uri";
    public static final String NO_IMAGE_EXTRA = "no_image";
    public static final String CHAT_UID_EXTRA = "uid";

    private View imageDialogDetails;
    private TextView imageDialogChatName;
    private ImageView imageDialogProfilePicture;
    private ImageView imageDialogRedirectChat;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        setContentView(R.layout.image_dialog_layout);

        imageDialogDetails = findViewById(R.id.image_dialog_chat_actions);
        imageDialogProfilePicture = findViewById(R.id.image_dialog_chat_profile_picture);
        imageDialogRedirectChat = findViewById(R.id.image_dialog_chat_redirect_button);
        imageDialogChatName = findViewById(R.id.image_dialog_chat_name);

        final String receiverUid = getIntent().getStringExtra(CHAT_UID_EXTRA);
        final String chatName = getIntent().getStringExtra(CHAT_NAME_EXTRA);
        String imageUri = getIntent().getStringExtra(IMAGE_URI_EXTRA);

        imageDialogRedirectChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageDialogActivity.this, ChatActivity.class);
                intent.putExtra("clicked", chatName);
                intent.putExtra("uid", receiverUid);
                SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager("thumbInfoLocal", ImageDialogActivity.this);
                intent.putExtra("friendThumb", sharedPreferenceManager.getData(receiverUid));
                sharedPreferenceManager = new SharedPreferenceManager("picInfoLocal", ImageDialogActivity.this);
                intent.putExtra("friendProfilePic", sharedPreferenceManager.getData(receiverUid));
                ImageDialogActivity.this.startActivity(intent);
            }
        });


        imageDialogChatName.setText(chatName);

        Picasso.get().load(Uri.parse(imageUri)).placeholder(R.drawable.ic_person_black_24dp).into(imageDialogProfilePicture, new Callback() {
            @Override
            public void onSuccess() {
                startPostponedEnterTransition();
            }

            @Override
            public void onError(Exception e) {
            }
        });

        final ViewGroup viewGroup = findViewById(android.R.id.content);

        TransitionManager.beginDelayedTransition(viewGroup);
        imageDialogDetails.setVisibility(View.VISIBLE);
    }

}
