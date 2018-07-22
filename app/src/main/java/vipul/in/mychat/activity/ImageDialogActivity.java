package vipul.in.mychat.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import vipul.in.mychat.R;
import vipul.in.mychat.util.Constants;

public class ImageDialogActivity extends AppCompatActivity {

    public static final String CHAT_NAME_EXTRA = "chat_name";
    public static final String IMAGE_URI_EXTRA = "chat_profile_image_uri";
    public static final String IMAGE_EXTRA = "imamge";
    private View imageDialogDetails;
    private TextView imageDialogChatName;
    private ImageView imageDialogProfilePicture;
    private ImageView imageDialogRedirectChat;
    private View imageDialog;
    private ViewGroup viewGroup;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        postponeEnterTransition();
        setContentView(R.layout.image_dialog_layout);

        imageDialogDetails = findViewById(R.id.image_dialog_chat_actions);
        imageDialogProfilePicture = findViewById(R.id.image_dialog_chat_profile_picture);
        imageDialogRedirectChat = findViewById(R.id.image_dialog_chat_redirect_button);
        imageDialogChatName = findViewById(R.id.image_dialog_chat_name);
        imageDialog = findViewById(R.id.image_dialog);

        final String receiverUid = getIntent().getStringExtra(Constants.RECEIVER_UID_EXTRA);
        final String chatName = getIntent().getStringExtra(CHAT_NAME_EXTRA);
        final String imageUri = getIntent().getStringExtra(IMAGE_URI_EXTRA);

        imageDialogRedirectChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ImageDialogActivity.this, ChatActivity.class);
                intent.putExtra("clicked", chatName);
                intent.putExtra("uid", receiverUid);
                intent.putExtra("friendThumb", imageUri);
                intent.putExtra("friendProfilePic", imageUri);
                ImageDialogActivity.this.startActivity(intent);
            }
        });

        imageDialogChatName.setText(chatName);

        Picasso.get()
                .load(Uri.parse(imageUri))
                .placeholder(R.drawable.ic_person_black_24dp)
                .into(imageDialogProfilePicture);

        startPostponedEnterTransition();

        viewGroup = findViewById(android.R.id.content);

        // TransitionManager.beginDelayedTransition(viewGroup);
        imageDialogDetails.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        supportFinishAfterTransition();
        super.onBackPressed();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        Rect imageDialogRect = new Rect();
        imageDialog.getGlobalVisibleRect(imageDialogRect);
        // If the Point (x, y) is not inside the Image Dialog
        if (!imageDialogRect.contains((int) ev.getRawX(), (int) ev.getRawY())) {
            // Finish the Activity after reversing SharedElement transition
            // TransitionManager.beginDelayedTransition(viewGroup);
            imageDialogDetails.setVisibility(View.GONE);
            supportFinishAfterTransition();
        }
        return super.dispatchTouchEvent(ev);
    }
}
