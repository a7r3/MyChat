package vipul.in.mychat;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class MarginDividerItemDecoration extends RecyclerView.ItemDecoration {

    private static final int[] ATTRS = new int[]{android.R.attr.listDivider};

    private Drawable divider;
    private Context context;

    public MarginDividerItemDecoration(Context context) {
        final TypedArray styledAttributes = context.obtainStyledAttributes(ATTRS);
        divider = styledAttributes.getDrawable(0);
        this.context = context;
        styledAttributes.recycle();
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = (int) Utils.convertDpToPixel(64.0f, context);
        int rightSub = (int) Utils.convertDpToPixel(16.0f, context);
        c.save();
        final int childCount = parent.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = parent.getChildAt(i);
            final int bottom = child.getBottom();
            final int top = bottom - divider.getIntrinsicHeight();
            int right = child.getWidth() - rightSub;
            divider.setBounds(left, top, right, bottom);
            divider.draw(c);
        }
        c.restore();
    }
}