package org.telegram.ui.Components.voip;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.Components.LayoutHelper;

public class EllipsizeTextView extends LinearLayout {

    private final TextView textView;
    private final EllipsizeView ellipsizeView;
    private final ValueAnimator ellipsizeAnimator;
    private boolean ellipsis;

    public EllipsizeTextView(@NonNull Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(Gravity.CENTER);
        textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        textView.setShadowLayer(AndroidUtilities.dp(3), 0, AndroidUtilities.dp(.666666667f), 0x4C000000);
        textView.setTextColor(Color.WHITE);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);
        addView(textView);

        ellipsizeView = new EllipsizeView(context);
        addView(ellipsizeView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, 0, 4, 2, 0, 0));

        int duration = 250;
        float frameCount = 60f / (1000f / duration);
        ellipsizeAnimator = ValueAnimator.ofFloat(0f, (frameCount - 1) / frameCount);
        ellipsizeAnimator.setDuration(duration);
        ellipsizeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        ellipsizeAnimator.addUpdateListener(value -> {
            ellipsizeView.animationProgress = (float) value.getAnimatedValue();
            ellipsizeView.invalidate();
        });
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (ellipsis) {
            ellipsizeAnimator.start();
        }
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        ellipsizeAnimator.cancel();
    }

    public void setText(CharSequence text) {
        textView.setText(text);
    }

    public void setText(CharSequence text, boolean ellipsis) {
        textView.setText(text);
        this.ellipsis = ellipsis;
        if (ellipsis) {
            ellipsizeView.setVisibility(VISIBLE);
            if (!ellipsizeAnimator.isStarted()) {
                ellipsizeAnimator.start();
            }
        } else {
            if (ellipsizeAnimator.isStarted()) {
                ellipsizeAnimator.cancel();
            }
            ellipsizeView.setVisibility(GONE);
        }
    }

    public CharSequence getText() {
        return textView.getText();
    }
}
