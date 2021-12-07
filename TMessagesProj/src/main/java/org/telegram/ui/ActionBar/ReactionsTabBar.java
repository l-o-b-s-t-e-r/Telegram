package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

public class ReactionsTabBar extends FrameLayout {

    private TextPaint countPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint emojiPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    public Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int paddingBetweenEmoji = AndroidUtilities.dp(6);
    private int paddingLeftRight = AndroidUtilities.dp(8);
    private int margin = 6;
    private int totalHeight = 28;
    private int borderWidth = AndroidUtilities.dp(2);
    private int cornersRadius = AndroidUtilities.dp(16);

    private int countSize = 12;
    private int emojiSize = 14;

    private HorizontalScrollView scrollView;
    private LinearLayout linearLayout;
    private View selectedView;

    public ReactionsTabBar(Context context, List<TLRPC.TL_reactionCount> reactionsCount, ReactionsMenuItem.OnReactionClickListener onReactionClickListener) {
        super(context);

        countPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlueText));
        backgroundPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlueBackground));
        borderPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlueText));

        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        countPaint.setTextSize(countSize);
        countPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        emojiPaint.setTextSize(emojiSize);
        emojiPaint.setColor(Color.WHITE);

        try {
            scrollView = new HorizontalScrollView(context);
            scrollView.setHorizontalScrollBarEnabled(false);

            addView(scrollView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.START));
        } catch (Throwable e) {
            e.printStackTrace();
            FileLog.e(e);
        }

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setPadding(AndroidUtilities.dp(margin), AndroidUtilities.dp(margin), 0, AndroidUtilities.dp(margin));
        for (int i = 0; i < reactionsCount.size(); i++) {
            linearLayout.addView(createButton(reactionsCount.get(i), onReactionClickListener, i == 0), LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, totalHeight, 0, 0, 0, margin, 0));

        }

        if (scrollView != null) {
            scrollView.addView(linearLayout, LayoutHelper.createFrame(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        } else {
            addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.START));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(heightMeasureSpec, MeasureSpec.AT_MOST));
    }

    private View createButton(TLRPC.TL_reactionCount reaction, ReactionsMenuItem.OnReactionClickListener onReactionClickListener, boolean chosen) {
        LinearLayout reactionButton = new LinearLayout(getContext());
        reactionButton.setOrientation(LinearLayout.HORIZONTAL);
        reactionButton.setPadding(paddingLeftRight, 0, paddingLeftRight, 0);
        reactionButton.setGravity(Gravity.CENTER_VERTICAL);
        setChosen(reactionButton, chosen);

        if (reaction.reaction.isEmpty()) {
            ImageView icon = new ImageView(getContext());
            icon.setImageResource(R.drawable.msg_reactions_filled);
            icon.setColorFilter(borderPaint.getColor());
            reactionButton.addView(icon);
        } else {
            TextView reactionText = new TextView(getContext());
            reactionText.setText(reaction.reaction);
            reactionText.setTextSize(emojiPaint.getTextSize());
            reactionText.setTextColor(emojiPaint.getColor());
            reactionButton.addView(reactionText);
        }

        TextView countText = new TextView(getContext());
        countText.setText(" " + AndroidUtilities.formatWholeNumber(reaction.count, 0));
        countText.setTextSize(countPaint.getTextSize());
        countText.setTextColor(countPaint.getColor());
        countText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        reactionButton.addView(countText);

        reactionButton.setOnClickListener(view -> {
            onReactionClickListener.onClick(reaction.reaction);
            setChosen(selectedView, false);
            setChosen(view, true);
        });

        return reactionButton;
    }

    public void setChosen(View view, boolean chosen) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(backgroundPaint.getColor());
        shape.setCornerRadius(cornersRadius);

        if (chosen) {
            shape.setStroke(borderWidth, borderPaint.getColor());
            selectedView = view;
        }

        view.setBackground(shape);
    }
}
