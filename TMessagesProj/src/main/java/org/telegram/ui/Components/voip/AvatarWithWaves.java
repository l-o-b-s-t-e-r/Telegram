package org.telegram.ui.Components.voip;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.SharedConfig;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.BlobDrawable;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.WaveDrawable;

public class AvatarWithWaves extends FrameLayout {

    private AvatarWavesDrawable avatarWavesDrawable;
    private BackupImageView avatarImageView;
    private AvatarDrawable avatarDrawable;
    private TLRPC.User currentUser;
    private int minSmallWaveRadius, wavesDifference;
    private int wavesAmplitudeDifference;
    private int avatarRadius;
    private ValueAnimator stopWavesAnimator;

    float stopWaveRadius, stopWaveRadius2;

    public static float MAX_AMPLITUDE = 2f;
    private float shift, amplitude;

    public AvatarWavesDrawable getAvatarWavesDrawable() {
        return avatarWavesDrawable;
    }

    public void setDrawAvatar(boolean draw) {
        if (avatarImageView.getImageReceiver().getVisible() != draw) {
            avatarImageView.getImageReceiver().setVisible(draw, true);
        }
    }

    public AvatarWithWaves(Context context, int avatarSize) {
        super(context);
        this.avatarRadius = AndroidUtilities.dp(avatarSize) / 2;
        avatarDrawable = new AvatarDrawable();
        setClipChildren(false);

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(avatarRadius);
        addView(avatarImageView, LayoutHelper.createFrame(avatarSize, avatarSize, Gravity.CENTER));

        minSmallWaveRadius = avatarRadius + AndroidUtilities.dp(6);
        int maxSmallWaveRadius = avatarRadius + AndroidUtilities.dp(18);

        int minAmplitude = AndroidUtilities.dp(1);
        int maxAmplitude = AndroidUtilities.dp(8);
        wavesAmplitudeDifference = maxAmplitude - minAmplitude;

        wavesDifference = maxSmallWaveRadius - minSmallWaveRadius;
        avatarWavesDrawable = new AvatarWavesDrawable();

        shift = wavesDifference / MAX_AMPLITUDE;
        amplitude = wavesAmplitudeDifference / MAX_AMPLITUDE;

        float minRadius = minSmallWaveRadius + shift * 1;
        stopWaveRadius = minRadius + amplitude * (1 + 1);
        float minRadius2 = minRadius + minRadius - avatarRadius;
        stopWaveRadius2 = minRadius2 + amplitude * (1 + 1);

        setWavesSize(0);
        setWillNotDraw(false);
        setFocusable(true);
    }

    //0 - 2
    public void setWavesSize(float size) {
        if (stopWavesAnimator != null && stopWavesAnimator.isRunning()) {
            stopWavesAnimator.cancel();
        }

        float minRadius = minSmallWaveRadius + shift * size;
        float maxRadius = minRadius + amplitude * (size + 1);
        float minRadius2 = minRadius + minRadius - avatarRadius;
        float maxRadius2 = minRadius2 + amplitude * (size + 1);

        avatarWavesDrawable.setRadius((int) minRadius, (int) maxRadius, (int) minRadius2, (int) maxRadius2);
    }

    public void stopWaves() {
        if (stopWavesAnimator == null || !stopWavesAnimator.isRunning()) {
            stopWavesAnimator = ValueAnimator.ofFloat(0f, 1f);
            float minRadius = avatarWavesDrawable.blobDrawable.minRadius;
            float maxRadius = avatarWavesDrawable.blobDrawable.maxRadius;
            float minRadius2 = avatarWavesDrawable.blobDrawable2.minRadius;
            float maxRadius2 = avatarWavesDrawable.blobDrawable2.maxRadius;

            float minRadiusDelta = (Math.max(minRadius, stopWaveRadius) - Math.min(minRadius, stopWaveRadius));
            minRadiusDelta = minRadius > stopWaveRadius ? -1f * minRadiusDelta : minRadiusDelta;

            float maxRadiusDelta = (Math.max(maxRadius, stopWaveRadius) - Math.min(maxRadius, stopWaveRadius));
            maxRadiusDelta = maxRadius > stopWaveRadius ? -1f * maxRadiusDelta : maxRadiusDelta;

            float minRadius2Delta = (Math.max(minRadius2, stopWaveRadius2) - Math.min(minRadius2, stopWaveRadius2));
            minRadius2Delta = minRadius2 > stopWaveRadius2 ? -1f * minRadius2Delta : minRadius2Delta;

            float maxRadius2Delta = (Math.max(maxRadius2, stopWaveRadius2) - Math.min(maxRadius2, stopWaveRadius2));
            maxRadius2Delta = maxRadius2 > stopWaveRadius2 ? -1f * maxRadius2Delta : maxRadius2Delta;

            final float finalMinRadiusDelta = minRadiusDelta;
            final float finalMaxRadiusDelta = maxRadiusDelta;
            final float finalMinRadius2Delta = minRadius2Delta;
            final float finalMaxRadius2Delta = maxRadius2Delta;

            stopWavesAnimator.addUpdateListener(valueAnimator -> {
                float progress = (float) valueAnimator.getAnimatedValue();
                avatarWavesDrawable.setRadius(
                        (int) (minRadius + finalMinRadiusDelta * progress),
                        (int) (maxRadius + finalMaxRadiusDelta * progress),
                        (int) (minRadius2 + finalMinRadius2Delta * progress),
                        (int) (maxRadius2 + finalMaxRadius2Delta * progress)
                );
            });

            stopWavesAnimator.setDuration(2000).start();
        }
    }

    public void stopWavesImmediately() {
        if (stopWavesAnimator != null && stopWavesAnimator.isRunning()) {
            stopWavesAnimator.cancel();
        }

        avatarWavesDrawable.setRadius(
                (int) stopWaveRadius,
                (int) (stopWaveRadius),
                (int) (stopWaveRadius2),
                (int) (stopWaveRadius2)
        );
    }

    public boolean hasAvatarSet() {
        return avatarImageView.getImageReceiver().hasNotThumb();
    }

    public void setData(TLRPC.User user) {
        currentUser = user;
        avatarDrawable.setInfo(currentUser);
        //avatarImageView.getImageReceiver().setCurrentAccount(account.getCurrentAccount());
        //avatarImageView.setImage(ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), null, Theme.createCircleDrawable(AndroidUtilities.dp(135), 0xFF000000), user);
        avatarImageView.setImage(ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), "50_50", avatarDrawable, 0xFF000000);

        //avatarImageView.setImage(ImageLocation.getForLocal(uploadingAvatar), "50_50", avatarDrawable, null);
        //ImageLocation imageLocation = ImageLocation.getForUser(currentUser, ImageLocation.TYPE_SMALL);
        //avatarImageView.setImage(imageLocation, "50_50", avatarDrawable, currentUser);
        //avatarImageView.setImage(ImageLocation.getForUserOrChat(user, ImageLocation.TYPE_SMALL), null, Theme.createCircleDrawable(AndroidUtilities.dp(135), 0xFF000000), user);

    }

    /*public void setAmplitude(double value) {
        if (value > 1.5f) {
            avatarWavesDrawable.setAmplitude(value);
        } else {
            avatarWavesDrawable.setAmplitude(0);
        }
    }*/

    @Override
    public boolean hasOverlappingRendering() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        avatarWavesDrawable.update();
        avatarWavesDrawable.draw(canvas, getMeasuredWidth() / 2f, getMeasuredHeight() / 2f, this);

        //avatarImageView.setScaleX(avatarWavesDrawable.getAvatarScale());
        //avatarImageView.setScaleY(avatarWavesDrawable.getAvatarScale());

        super.dispatchDraw(canvas);
    }

    public void getAvatarPosition(int[] pos) {
        avatarImageView.getLocationInWindow(pos);
    }

    public static class AvatarWavesDrawable {

        float amplitude;
        float animateToAmplitude;
        float animateAmplitudeDiff;
        float wavesEnter = 0f;
        boolean showWaves = true;

        private BlobDrawable blobDrawable;
        private BlobDrawable blobDrawable2;

        private boolean hasCustomColor;
        private int isMuted;
        private float progressToMuted = 0;

        boolean invalidateColor = true;

        public AvatarWavesDrawable() {
            blobDrawable = new BlobDrawable(6);
            blobDrawable2 = new BlobDrawable(8);
            blobDrawable.paint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_voipgroup_speakingText), (int) 36));
            blobDrawable2.paint.setColor(ColorUtils.setAlphaComponent(Theme.getColor(Theme.key_voipgroup_speakingText), (int) 20));
        }

        public void update() {
            if (animateToAmplitude != amplitude) {
                amplitude += animateAmplitudeDiff * 16;
                if (animateAmplitudeDiff > 0) {
                    if (amplitude > animateToAmplitude) {
                        amplitude = animateToAmplitude;
                    }
                } else {
                    if (amplitude < animateToAmplitude) {
                        amplitude = animateToAmplitude;
                    }
                }
            }

            if (showWaves && wavesEnter != 1f) {
                wavesEnter += 16 / 350f;
                if (wavesEnter > 1f) {
                    wavesEnter = 1f;
                }
            } else if (!showWaves && wavesEnter != 0) {
                wavesEnter -= 16 / 350f;
                if (wavesEnter < 0f) {
                    wavesEnter = 0f;
                }
            }
        }

        public void draw(Canvas canvas, float cx, float cy, View parentView) {
            if (SharedConfig.getLiteMode().enabled()) {
                return;
            }
            //float scaleBlob = 1.0f + 0.4f * amplitude;
            if (showWaves || wavesEnter != 0) {
                canvas.save();
                float wavesEnter = CubicBezierInterpolator.DEFAULT.getInterpolation(this.wavesEnter);

                //canvas.scale(scaleBlob * wavesEnter, scaleBlob * wavesEnter, cx, cy);

                if (!hasCustomColor) {
                    if (isMuted != 1 && progressToMuted != 1f) {
                        progressToMuted += 16 / 150f;
                        if (progressToMuted > 1f) {
                            progressToMuted = 1f;
                        }
                        invalidateColor = true;
                    } else if (isMuted == 1 && progressToMuted != 0f) {
                        progressToMuted -= 16 / 150f;
                        if (progressToMuted < 0f) {
                            progressToMuted = 0f;
                        }
                        invalidateColor = true;
                    }

                    if (invalidateColor) {
                        int color = ColorUtils.blendARGB(Theme.getColor(Theme.key_voipgroup_speakingText), isMuted == 2 ? Theme.getColor(Theme.key_voipgroup_mutedByAdminIcon) : Theme.getColor(Theme.key_voipgroup_listeningText), progressToMuted);
                        blobDrawable.paint.setColor(ColorUtils.setAlphaComponent(color, (int) (255 * WaveDrawable.CIRCLE_ALPHA_2)));
                    }
                }

                blobDrawable.update(amplitude, 1f);
                blobDrawable.draw(cx, cy, canvas, blobDrawable.paint);

                blobDrawable2.update(amplitude, 1f);
                blobDrawable2.draw(cx, cy, canvas, blobDrawable.paint);
                canvas.restore();
            }

            if (wavesEnter != 0) {
                parentView.invalidate();
            }
        }

        public float getAvatarScale() {
            float scaleAvatar = 0.9f + 0.2f * amplitude;
            float wavesEnter = CubicBezierInterpolator.EASE_OUT.getInterpolation(this.wavesEnter);
            return scaleAvatar * wavesEnter + 1f * (1f - wavesEnter);
        }

        public void setShowWaves(boolean show, View parentView) {
            if (showWaves != show) {
                parentView.invalidate();
            }
            showWaves = show;
        }

        public void setAmplitude(double value) {
            float amplitude = (float) value / 80f;
            if (!showWaves) {
                amplitude = 0;
            }
            if (amplitude > 1f) {
                amplitude = 1f;
            } else if (amplitude < 0) {
                amplitude = 0;
            }
            animateToAmplitude = amplitude;
            animateAmplitudeDiff = (animateToAmplitude - this.amplitude) / 200;
        }

        public void setColor(int color) {
            hasCustomColor = true;
            blobDrawable.paint.setColor(color);
        }

        public void setMuted(int status, boolean animated) {
            this.isMuted = status;
            if (!animated) {
                progressToMuted = isMuted != 1 ? 1f : 0f;
            }
            invalidateColor = true;
        }

        public void setRadius(int minRadius, int maxRadius, int minRadius2, int maxRadius2) {
            blobDrawable.minRadius = minRadius;
            blobDrawable.maxRadius = maxRadius;
            blobDrawable2.minRadius = minRadius2;
            blobDrawable2.maxRadius = maxRadius2;
            blobDrawable.generateBlob();
            blobDrawable2.generateBlob();
        }
    }

    public BackupImageView getAvatarImageView() {
        return avatarImageView;
    }
}
