package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.PaintDrawable;
import android.os.Build;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.SvgHelper;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.LayoutHelper;

import java.util.List;

public class ReactionsMenuItem extends FrameLayout {

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private HorizontalScrollView scrollView;
    private LinearLayout linearLayout;
    private final Theme.ResourcesProvider resourcesProvider;

    public interface OnReactionClickListener {
        void onClick(String reaction);
    }

    public interface OnBigReactionClickListener {
        void onClick(TLRPC.TL_availableReaction reaction);
    }

    public ReactionsMenuItem(Context context, Theme.ResourcesProvider resourcesProvider, List<TLRPC.TL_availableReaction> reactions, OnBigReactionClickListener reactionClickListener) {
        super(context);
        this.resourcesProvider = resourcesProvider;
        setPadding(AndroidUtilities.dp(4), 0, 0, AndroidUtilities.dp(12 + 8));

        int color = Theme.getColor(Theme.key_actionBarDefaultSubmenuBackground);
        paint.setColor(color);

        PaintDrawable shape = new PaintDrawable(color);
        shape.setCornerRadius(AndroidUtilities.dp(24));

        try {
            scrollView = new HorizontalScrollView(context);
            scrollView.setHorizontalScrollBarEnabled(false);
            scrollView.setBackground(shape);

            addView(scrollView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.END));
        } catch (Throwable e) {
            e.printStackTrace();
            FileLog.e(e);
        }

        linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        for (TLRPC.TL_availableReaction reaction : reactions) {
            BackupImageView imageView = new BackupImageView(context);
            imageView.setAspectFit(true);
            imageView.setLayerNum(1);
            imageView.setBackgroundDrawable(Theme.createSelectorDrawable(getThemedColor(Theme.key_listSelector)));
            imageView.setOnClickListener(v -> {
                reactionClickListener.onClick(reaction);
            });

            setSticker(reaction.select_animation, imageView);
            linearLayout.addView(imageView, LayoutHelper.createFrame(32, 32, Gravity.CENTER_VERTICAL, 4, 8, 4, 8));
        }

        if (scrollView != null) {
            scrollView.addView(linearLayout, new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                scrollView.setClipToOutline(true);
            }
        } else {
            linearLayout.setBackground(shape);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                linearLayout.setClipToOutline(true);
            }
            addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL));
        }

        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int bigCircleCx = getMeasuredWidth() - AndroidUtilities.dp(48 - 8);
        int bigCircleCy = linearLayout.getMeasuredHeight() - AndroidUtilities.dp(2);
        int smallCircleCx = bigCircleCx + AndroidUtilities.dp(6);
        int smallCircleCy = bigCircleCy + AndroidUtilities.dp(12 + 2);

        canvas.drawCircle(bigCircleCx, bigCircleCy, AndroidUtilities.dp(8), paint);
        canvas.drawCircle(smallCircleCx, smallCircleCy, AndroidUtilities.dp(4), paint);

        super.onDraw(canvas);
    }

    private int getThemedColor(String key) {
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(key) : null;
        return color != null ? color : Theme.getColor(key);
    }

    public void setSticker(TLRPC.Document document, BackupImageView imageView) {
        if (document != null) {
            TLRPC.PhotoSize thumb = FileLoader.getClosestPhotoSizeWithSize(document.thumbs, 90);
            SvgHelper.SvgDrawable svgThumb = DocumentObject.getSvgThumb(document, Theme.key_windowBackgroundGray, 1.0f);
            if (MessageObject.canAutoplayAnimatedSticker(document)) {
                if (svgThumb != null) {
                    imageView.setImage(ImageLocation.getForDocument(document), "80_80", null, svgThumb, null);
                } else if (thumb != null) {
                    imageView.setImage(ImageLocation.getForDocument(document), "80_80", ImageLocation.getForDocument(thumb, document), null, 0, null);
                } else {
                    imageView.setImage(ImageLocation.getForDocument(document), "80_80", null, null, null);
                }
            } else {
                if (svgThumb != null) {
                    if (thumb != null) {
                        imageView.setImage(ImageLocation.getForDocument(thumb, document), null, "webp", svgThumb, null);
                    } else {
                        imageView.setImage(ImageLocation.getForDocument(document), null, "webp", svgThumb, null);
                    }
                } else {
                    imageView.setImage(ImageLocation.getForDocument(thumb, document), null, "webp", null, null);
                }
            }
        }
    }
}
