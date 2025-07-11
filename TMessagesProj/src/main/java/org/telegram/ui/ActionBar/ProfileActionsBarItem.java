package org.telegram.ui.ActionBar;

import static org.telegram.messenger.AndroidUtilities.readRes;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.R;
import org.telegram.messenger.SvgHelper;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

public class ProfileActionsBarItem extends LinearLayout {

    private TextView textView;

    private ImageView iconView;

    private int position;

    private int priority;

    private boolean isDark;

    public ProfileActionsBarItem(Context context) {
        super(context);
        int padding = AndroidUtilities.dp(8);

        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER_HORIZONTAL);
        setPadding(padding, padding, padding, padding);

        iconView = new ImageView(context);
        //iconView.setScaleType(ImageView.ScaleType.CENTER);
        //iconView.setPadding(0, 0, 0, AndroidUtilities.dp(8));
        addView(iconView, LayoutHelper.createFrame(24, 24, Gravity.CENTER, 0, 0, 0, 4));

        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 10);
        textView.setTypeface(AndroidUtilities.bold());
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER);
        textView.setMaxLines(1);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT));
    }

    public void setIsDark(boolean isDark) {
        GradientDrawable backgroundDrawable = new GradientDrawable();
        backgroundDrawable.setColor(isDark ? 0x1F000000 : 0x1FFFFFFF);
        backgroundDrawable.setCornerRadius(AndroidUtilities.dp(12));
        setBackground(backgroundDrawable);
        invalidate();
    }

    public void setText(CharSequence text) {
        if (textView == null) {
            return;
        }
        textView.setText(text);
    }

    public void setIcon(int resId) {
        if (iconView == null) {
            return;
        }

        iconView.setImageResource(resId);
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setScale(float scale) {
        if (iconView != null) {
            iconView.setScaleX(scale);
            iconView.setScaleY(scale);
        }
        if (textView != null) {
            textView.setScaleX(scale);
            textView.setScaleY(scale);
        }
        setPivotY(getMeasuredHeight());
        setScaleY(scale);
        setAlpha(scale);
    }
}
