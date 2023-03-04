package org.telegram.ui.Components.voip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RLottieImageView;

public class VoIPToggleButton2 extends FrameLayout {

    Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Drawable staticIconDrawable;

    FrameLayout textLayoutContainer;
    TextView[] textView = new TextView[2];

    float replaceProgress;
    ValueAnimator replaceAnimator;

    int currentStaticIcon;
    int currentAnimatedIcon;
    int currentIconColor;
    int currentBackgroundColor;
    int previousBackgroundColor;
    String currentText;
    public int animationDelay;

    private Paint xRefPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    Drawable rippleDrawable;

    private Paint bitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private int radius;
    private int circleSize;

    private RLottieImageView lottieImageView;
    private Bitmap gradientBitmap;
    private Bitmap iconGradientBitmap;
    private float parentWidth;
    private float parentHeight;
    final int[] locationOnScreen = new int[2];

    private int iconState;
    private int iconGroup;

    public static final int GRADIENT_ICON = 1; //gradient
    public static final int SOLID_ICON = 2; //white or red

    public static final int SPEAKER_GROUP = 1;
    public static final int CAMERA_FLIP_GROUP = 2;

    public VoIPToggleButton2(@NonNull Context context) {
        this(context, 52);
    }

    public VoIPToggleButton2(@NonNull Context context, int radius) {
        super(context);
        this.radius = radius;
        circleSize = AndroidUtilities.dp(radius);
        setWillNotDraw(false);

        textLayoutContainer = new FrameLayout(context);
        addView(textLayoutContainer);

        for (int i = 0; i < 2; i++) {
            TextView textView = new TextView(context);
            textView.setGravity(Gravity.CENTER_HORIZONTAL);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 11);
            textView.setTextColor(Color.WHITE);
            textView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
            textLayoutContainer.addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, radius + 4, 0, 0));
            this.textView[i] = textView;
        }
        textView[1].setVisibility(View.GONE);


        xRefPaint.setColor(0xff000000);
        xRefPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        xRefPaint.setStrokeWidth(AndroidUtilities.dp(3));

        bitmapPaint.setFilterBitmap(true);

        lottieImageView = new RLottieImageView(context);
        addView(lottieImageView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));
    }

    public void setTextSize(int size) {
        for (int i = 0; i < 2; i++) {
            textView[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, size);
        }
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        getLocationOnScreen(locationOnScreen);
        float cx = getWidth() / 2f;
        float cy = circleSize / 2f;

        if (iconState == GRADIENT_ICON) {
            currentIconColor = getIconGradientColor();
            setupAnimatedIconColor(currentIconColor);
        }

        if (replaceProgress != 0 && previousBackgroundColor != currentBackgroundColor) {
            int size = (int) (circleSize * (1f - replaceProgress));
            drawCircle(previousBackgroundColor, size, cx, cy, canvas);
            size = (int) (circleSize * replaceProgress);
            drawCircle(currentBackgroundColor, size, cx, cy, canvas);
        } else {
            drawCircle(currentBackgroundColor, circleSize, cx, cy, canvas);
        }

        if (rippleDrawable == null) {
            rippleDrawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(this.radius), 0, Color.BLACK);
            rippleDrawable.setCallback(this);
        }
        rippleDrawable.setBounds((int) (cx - radius), (int) (cy - radius), (int) (cx + radius), (int) (cy + radius));
        rippleDrawable.draw(canvas);

        if (currentStaticIcon != 0 && staticIconDrawable != null) {
            canvas.save();
            staticIconDrawable.setBounds(
                    (int) (cx - staticIconDrawable.getIntrinsicWidth() / 2f), (int) (cy - staticIconDrawable.getIntrinsicHeight() / 2f),
                    (int) (cx + staticIconDrawable.getIntrinsicWidth() / 2f), (int) (cy + staticIconDrawable.getIntrinsicHeight() / 2f)
            );
            staticIconDrawable.draw(canvas);

            canvas.restore();
        }
    }

    private void drawCircle(int backgroundColor, int size, float cx, float cy, Canvas canvas) {
        if (size > 0 && gradientBitmap != null && backgroundColor == 0) {
            drawGradientBackground(size, cx, cy, canvas);
        } else {
            circlePaint.setColor(backgroundColor);
            canvas.drawCircle(cx, cy, size / 2f, circlePaint);
        }
    }

    private void drawGradientBackground(int size, float cx, float cy, Canvas canvas) {
        final float radius = size / 2f;
        final Paint resultPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        resultPaint.setColor(Color.WHITE);

        final Bitmap circleBitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas circleCanvas = new Canvas(circleBitmap);
        circleCanvas.drawCircle(radius, radius, radius, resultPaint);

        resultPaint.setFilterBitmap(false);
        resultPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        Matrix matrix = new Matrix();
        final float sx = parentWidth / gradientBitmap.getWidth();
        final float sy = parentHeight / gradientBitmap.getHeight();
        final float px = gradientBitmap.getWidth() * (locationOnScreen[0] / parentWidth);
        final float py = gradientBitmap.getHeight() * (locationOnScreen[1] / parentHeight);
        matrix.setScale(sx, sy, px, py);

        circleCanvas.drawBitmap(gradientBitmap, matrix, resultPaint);
        canvas.drawBitmap(circleBitmap, cx - radius, cy - radius, null);
    }

    public void setEnabled(boolean enabled, boolean animated) {
        super.setEnabled(enabled);
        if (animated) {
            animate().alpha(enabled ? 1.0f : 0.5f).setDuration(180).start();
        } else {
            clearAnimation();
            setAlpha(enabled ? 1.0f : 0.5f);
        }
    }

    public void setData(int staticIcon, int animatedIcon, int iconColor, int backgroundColor, int state, String text, boolean animated) {
        setData(staticIcon, animatedIcon, iconColor, backgroundColor, state, text, animated, 0);
    }

    public void setData(int staticIcon, int animatedIcon, int iconColor, int backgroundColor, int state, String text, boolean animated, int group) {
        if (getVisibility() != View.VISIBLE) {
            animated = false;
            setVisibility(View.VISIBLE);
        }

        if (iconState == state && currentStaticIcon == staticIcon && currentAnimatedIcon == animatedIcon && /*currentIconColor == iconColor &&*/ (currentBackgroundColor == backgroundColor) && (currentText != null && currentText.equals(text)) && group == iconGroup) {
            return;
        }

        if (Color.alpha(backgroundColor) == 255 && AndroidUtilities.computePerceivedBrightness(backgroundColor) > 0.5) {
            rippleDrawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(radius), 0, ColorUtils.setAlphaComponent(Color.BLACK, (int) (255 * 0.1f * 1.0f)));
            rippleDrawable.setCallback(this);
        } else {
            rippleDrawable = Theme.createSimpleSelectorCircleDrawable(AndroidUtilities.dp(radius), 0, ColorUtils.setAlphaComponent(Color.WHITE, (int) (255 * 0.3f * 1.0f)));
            rippleDrawable.setCallback(this);
        }

        if (replaceAnimator != null) {
            replaceAnimator.cancel();
        }

        currentIconColor = iconColor;
        previousBackgroundColor = currentBackgroundColor;
        currentBackgroundColor = backgroundColor;
        currentText = text;
        iconState = state;

        if (staticIcon != 0) {
            currentStaticIcon = staticIcon;
            staticIconDrawable = ContextCompat.getDrawable(getContext(), staticIcon).mutate();
            staticIconDrawable.setColorFilter(new PorterDuffColorFilter(iconColor, PorterDuff.Mode.MULTIPLY));
            textView[0].setText(text);
            replaceProgress = 0f;
            invalidate();
        }

        if (animated) {
            boolean animateText = !textView[0].getText().toString().equals(text);

            if (!animateText) {
                textView[0].setText(text);
            } else {
                textView[1].setText(text);
                textView[1].setVisibility(View.VISIBLE);
                textView[1].setAlpha(0);
                textView[1].setScaleX(0);
                textView[1].setScaleY(0);
            }
            replaceAnimator = ValueAnimator.ofFloat(0, 1f);
            replaceAnimator.addUpdateListener(valueAnimator -> {
                replaceProgress = (float) valueAnimator.getAnimatedValue();
                invalidate();

                if (animateText) {
                    textView[0].setAlpha(1f - replaceProgress);
                    textView[0].setScaleX(1f - replaceProgress);
                    textView[0].setScaleY(1f - replaceProgress);

                    textView[1].setAlpha(replaceProgress);
                    textView[1].setScaleX(replaceProgress);
                    textView[1].setScaleY(replaceProgress);
                }
            });
            replaceAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    replaceAnimator = null;
                    if (animateText) {
                        TextView tv = textView[0];
                        textView[0] = textView[1];
                        textView[1] = tv;
                        textView[1].setVisibility(View.GONE);
                    }
                    replaceProgress = 0f;
                    invalidate();
                }
            });
            replaceAnimator.setDuration(150).start();
        } else {
            textView[0].setText(text);
        }

        if (animatedIcon != 0) {
            setupAnimatedIconColor(currentIconColor);
            if (animated && animatedIcon != currentAnimatedIcon && iconGroup == group) {
                lottieImageView.setOnAnimationEndListener(() -> {
                    lottieImageView.setAnimation(currentAnimatedIcon, radius, radius);
                });
                lottieImageView.playAnimation();
            } else {
                lottieImageView.setAnimation(animatedIcon, radius, radius);
            }
            iconGroup = group;
            currentAnimatedIcon = animatedIcon;
        }

        invalidate();
    }

    private void setupAnimatedIconColor(int color) {
        lottieImageView.setLayerColor("Call Unmute Outlines.**", color);
        lottieImageView.setLayerColor("Call Mute Outlines.**", color);

        lottieImageView.setLayerColor("base.**", color);
        lottieImageView.setLayerColor("slash.**", color);

        lottieImageView.setLayerColor("wave1.**", color);
        lottieImageView.setLayerColor("wave2.**", color);
        lottieImageView.setLayerColor("Speaker Base.**", color);

        lottieImageView.setLayerColor("Camera Flip Outlines.**", color);
    }

    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        if (rippleDrawable != null) {
            rippleDrawable.setState(getDrawableState());
        }
    }

    @Override
    public boolean verifyDrawable(Drawable drawable) {
        return rippleDrawable == drawable || super.verifyDrawable(drawable);
    }

    @Override
    public void jumpDrawablesToCurrentState() {
        super.jumpDrawablesToCurrentState();
        if (rippleDrawable != null) {
            rippleDrawable.jumpToCurrentState();
        }
    }

    public void shakeView() {
        AndroidUtilities.shakeView(textView[0]);
        AndroidUtilities.shakeView(textView[1]);
    }

    public void showText(boolean show, boolean animated) {
        if (animated) {
            float a = show ? 1f : 0;
            if (textLayoutContainer.getAlpha() != a) {
                textLayoutContainer.animate().alpha(a).start();
            }
        } else {
            textLayoutContainer.animate().cancel();
            textLayoutContainer.setAlpha(show ? 1f : 0);
        }
    }

    public Bitmap getGradientBitmap() {
        return gradientBitmap;
    }

    public void setGradientBitmap(Bitmap gradientBitmap) {
        this.gradientBitmap = gradientBitmap;
    }

    public void setIconGradientBitmap(Bitmap iconGradientBitmap) {
        this.iconGradientBitmap = iconGradientBitmap;
    }

    /*public void setIconBackground(int color) {
        this.currentIconColor = color;
    }*/

    public void setParentSize(int width, int height) {
        parentWidth = width;
        parentHeight = height;
    }

    public int[] getLocationOnScreen() {
        return locationOnScreen;
    }

    public boolean isSolidBackgroundState() {
        return iconState == SOLID_ICON;
    }

    private int getIconGradientColor() {
        if (iconGradientBitmap != null) {
            final float px = iconGradientBitmap.getWidth() * (locationOnScreen[0] / parentWidth);
            final float py = iconGradientBitmap.getHeight() * (locationOnScreen[1] / parentHeight);
            if (px >= iconGradientBitmap.getWidth() - 1 || py >= iconGradientBitmap.getHeight() - 1) {
                return 0;
            } else {
                return iconGradientBitmap.getPixel((int) px, (int) py);
            }
        } else {
            return 0;
        }
    }
}