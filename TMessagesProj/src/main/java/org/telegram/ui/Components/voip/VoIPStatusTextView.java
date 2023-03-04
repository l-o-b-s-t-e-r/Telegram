package org.telegram.ui.Components.voip;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;

public class VoIPStatusTextView extends FrameLayout {

    EllipsizeTextView[] textView = new EllipsizeTextView[2];
    EllipsizeTextView reconnectTextView;
    VoIPTimerView timerView;

    CharSequence nextTextToSet;
    boolean nextEllipsisToSet;
    boolean animationInProgress;

    ValueAnimator animator;
    boolean timerShowing;

    public VoIPStatusTextView(@NonNull Context context) {
        super(context);
        for (int i = 0; i < 2; i++) {
            textView[i] = new EllipsizeTextView(context);
            addView(textView[i]);
        }

        reconnectTextView = new EllipsizeTextView(context);
        addView(reconnectTextView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 0, 22, 0, 0));

        reconnectTextView.setText(LocaleController.getString("VoipReconnecting", R.string.VoipReconnecting));
        reconnectTextView.setVisibility(View.GONE);

        timerView = new VoIPTimerView(context);
        addView(timerView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
    }

    public void setText(String text, boolean ellipsis, boolean animated) {
        CharSequence nextString = text;
        if (TextUtils.isEmpty(textView[0].getText())) {
            animated = false;
        }

        if (!animated) {
            if (animator != null) {
                animator.cancel();
            }
            animationInProgress = false;
            textView[0].setText(nextString, ellipsis);
            textView[0].setVisibility(View.VISIBLE);
            textView[1].setVisibility(View.GONE);
            timerView.setVisibility(View.GONE);

        } else {
            if (animationInProgress) {
                nextTextToSet = nextString;
                nextEllipsisToSet = ellipsis;
                return;
            }

            if (timerShowing) {
                textView[0].setText(nextString, ellipsis);
                replaceViews(timerView, textView[0], null);
            } else {
                if (!textView[0].getText().equals(nextString)) {
                    textView[1].setText(nextString, ellipsis);
                    replaceViews(textView[0], textView[1], () -> {
                        EllipsizeTextView v = textView[0];
                        textView[0] = textView[1];
                        textView[1] = v;
                    });
                }
            }
        }
    }

    public void showTimer(boolean animated) {
        if (TextUtils.isEmpty(textView[0].getText())) {
            animated = false;
        }
        if (timerShowing) {
            return;
        }
        timerView.updateTimer();
        if (!animated) {
            if (animator != null) {
                animator.cancel();
            }
            timerShowing = true;
            animationInProgress = false;
            textView[0].setVisibility(View.GONE);
            textView[1].setVisibility(View.GONE);
            timerView.setVisibility(View.VISIBLE);
        } else {
            if (animationInProgress) {
                nextTextToSet = "timer";
                nextEllipsisToSet = false;
                return;
            }
            timerShowing = true;
            replaceViews(textView[0], timerView, null);
        }
    }


    private void replaceViews(View out, View in, Runnable onEnd) {
        out.setVisibility(View.VISIBLE);
        in.setVisibility(View.VISIBLE);

        in.setTranslationY(AndroidUtilities.dp(15));
        in.setAlpha(0f);
        animationInProgress = true;
        animator = ValueAnimator.ofFloat(0, 1f);
        animator.addUpdateListener(valueAnimator -> {
            float v = (float) valueAnimator.getAnimatedValue();
            float inScale = 0.4f + 0.6f * v;
            float outScale = 0.4f + 0.6f * (1f - v);
            in.setTranslationY(AndroidUtilities.dp(10) * (1f - v));
            in.setAlpha(v);
            in.setScaleX(inScale);
            in.setScaleY(inScale);

            out.setTranslationY(-AndroidUtilities.dp(10) * v);
            out.setAlpha(1f - v);
            out.setScaleX(outScale);
            out.setScaleY(outScale);
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                out.setVisibility(View.GONE);
                out.setAlpha(1f);
                out.setTranslationY(0);
                out.setScaleY(1f);
                out.setScaleX(1f);

                in.setAlpha(1f);
                in.setTranslationY(0);
                in.setVisibility(View.VISIBLE);
                in.setScaleY(1f);
                in.setScaleX(1f);

                if (onEnd != null) {
                    onEnd.run();
                }
                animationInProgress = false;
                if (nextTextToSet != null) {
                    if (nextTextToSet.equals("timer")) {
                        showTimer(true);
                    } else {
                        textView[1].setText(nextTextToSet, nextEllipsisToSet);
                        replaceViews(textView[0], textView[1], () -> {
                            EllipsizeTextView v = textView[0];
                            textView[0] = textView[1];
                            textView[1] = v;
                        });
                    }
                    nextTextToSet = null;
                    nextEllipsisToSet = false;
                }
            }
        });
        animator.setDuration(250).setInterpolator(CubicBezierInterpolator.DEFAULT);
        animator.start();
    }

    public void setSignalBarCount(int count) {
        timerView.setSignalBarCount(count);
    }

    public int getSignalBarCount() {
        return  timerView.getSignalBarCount();
    }

    public void showReconnect(boolean showReconnecting, boolean animated) {
        if (!animated) {
            reconnectTextView.animate().setListener(null).cancel();
            reconnectTextView.setVisibility(showReconnecting ? View.VISIBLE : View.GONE);
        } else {
            if (showReconnecting) {
                if (reconnectTextView.getVisibility() != View.VISIBLE) {
                    reconnectTextView.setVisibility(View.VISIBLE);
                    reconnectTextView.setAlpha(0);
                }
                reconnectTextView.animate().setListener(null).cancel();
                reconnectTextView.animate().alpha(1f).setDuration(150).start();
            } else {
                reconnectTextView.animate().alpha(0).setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        reconnectTextView.setVisibility(View.GONE);
                    }
                }).setDuration(150).start();
            }
        }
    }
}
