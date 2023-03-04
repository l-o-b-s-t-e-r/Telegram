package org.telegram.ui.Components.voip;

import android.content.Context;
import android.graphics.Canvas;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

public class EmojiLayout extends FrameLayout {

    private final int emojiSize = 32;

    BackupImageView[] emojiViews = new BackupImageView[4];
    TLRPC.TL_availableReaction[] reactions = new TLRPC.TL_availableReaction[4];

    public EmojiLayout(Context context) {
        super(context);
        setWillNotDraw(false);
        for (int i = 0; i < emojiViews.length; i++) {
            emojiViews[i] = new BackupImageView(context);
            emojiViews[i].setAspectFit(true);
            emojiViews[i].setLayerNum(1);
            addView(emojiViews[i], LayoutHelper.createFrame(emojiSize, emojiSize));
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(
                AndroidUtilities.dp(emojiSize * emojiViews.length),
                AndroidUtilities.dp(emojiSize)
        );
    }

    @Override
    protected void onDraw(Canvas canvas) {
        for (int i = 0; i < emojiViews.length; i++) {
            emojiViews[i].setTranslationX(AndroidUtilities.dp(emojiSize) * i);
        }
    }

    public void setReaction(int index, TLRPC.TL_availableReaction reaction) {
        reactions[index] = reaction;
    }
}
