package org.telegram.ui.ActionBar;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.view.View;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.Emoji;
import org.telegram.messenger.ImageReceiver;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;

import java.util.ArrayList;
import java.util.List;

public class ReactionButton {

    private TLRPC.TL_reactionCount reactionCount;
    private TextPaint countPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    private TextPaint emojiPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    public Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private ImageReceiver[] reactionAvatarImages;
    private AvatarDrawable[] reactionAvatarDrawables;

    public StaticLayout reactionEmojiLayout;
    public StaticLayout reactionCountLayout;
    public ArrayList<TLRPC.TL_messageUserReaction> userReactions = new ArrayList<>();

    private int paddingBetweenEmoji = AndroidUtilities.dp(6);
    private int paddingLeftRight = AndroidUtilities.dp(8);
    private int margin = AndroidUtilities.dp(6);
    public int totalWidth;
    public int buttonWidth;
    public static int totalHeight = AndroidUtilities.dp(28 + 6);
    public static int borderWidth = AndroidUtilities.dp(2);
    public int buttonHeight = totalHeight - margin;
    public static int cornersRadius = AndroidUtilities.dp(16);
    public int x;
    public int row; //starts from 0

    private int reactedUserAvatarSize = AndroidUtilities.dp(24);
    private int reactedUserAvatarPadding = AndroidUtilities.dp(1f);
    private int reactedUserAvatarShift = AndroidUtilities.dp(12);
    private int maxTextWidth = AndroidUtilities.dp(64);
    private int countSize = AndroidUtilities.dp(12);
    private int emojiSize = AndroidUtilities.dp(14);
    private RectF rect;
    private boolean chosen;

    public void init(View view, int currentAccount, boolean showReactedUsers, TLRPC.TL_reactionCount reactionCount, MessageObject messageObject, boolean isMedia, ArrayList<TLRPC.User> users, ArrayList<TLRPC.TL_messageUserReaction> messageReactions) {
        this.reactionCount = reactionCount;
        this.chosen = reactionCount.chosen;
        if (messageObject.messageOwner.reactions != null && !messageObject.messageOwner.reactions.recent_reactons.isEmpty()) {
            for (TLRPC.TL_messageUserReaction reactedUser: messageObject.messageOwner.reactions.recent_reactons) {
                if (reactedUser.reaction.equals(getReaction())) {
                    userReactions.add(reactedUser);
                }
            }
        }

        if (messageReactions != null) {
            for (TLRPC.TL_messageUserReaction reactedUser : messageReactions) {
                if (reactedUser.reaction.equals(getReaction())) {
                    boolean found = false;
                    for (TLRPC.TL_messageUserReaction existing: userReactions) {
                        if (existing.user_id == reactedUser.user_id) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        userReactions.add(reactedUser);
                    }
                }
            }
        }

        if (isMedia) {
            countPaint.setColor(Theme.getColor(Theme.key_chat_reactionWhiteText));
            backgroundPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlackTransparentBackground));
            borderPaint.setColor(Theme.getColor(Theme.key_chat_reactionWhiteText));
        } else {
            if (messageObject.isOutOwner()) {
                countPaint.setColor(Theme.getColor(Theme.key_chat_outSentCheck)); //green
                backgroundPaint.setColor(Theme.getColor(Theme.key_chat_reactionGreenBackground));
                borderPaint.setColor(Theme.getColor(Theme.key_chat_outSentCheck)); //green
            } else {
                countPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlueText));
                backgroundPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlueBackground));
                borderPaint.setColor(Theme.getColor(Theme.key_chat_reactionBlueText)); //green
            }
        }
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(borderWidth);
        countPaint.setTextSize(countSize);
        countPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

        emojiPaint.setTextSize(emojiSize);
        emojiPaint.setColor(Color.WHITE);

        CharSequence emoji = "";
        CharSequence count = "";
        if (reactionCount != null) {
            if (reactionCount.count < 4 && showReactedUsers) {
                createAvatarsOffline(view, messageObject.currentAccount, reactionCount.reaction, userReactions, users);
                emoji = Emoji.replaceEmoji(reactionCount.reaction, emojiPaint.getFontMetricsInt(), emojiSize, false);
                totalWidth = reactedUserAvatarSize + reactedUserAvatarShift * (reactionCount.count - 1) - paddingLeftRight;
            } else {
                emoji = Emoji.replaceEmoji(reactionCount.reaction, emojiPaint.getFontMetricsInt(), emojiSize, false);
                count = AndroidUtilities.formatWholeNumber(reactionCount.count, 0);
            }
        }

        Rect bounds = new Rect();
        emojiPaint.getTextBounds(emoji.toString(), 0, emoji.length(), bounds);
        reactionEmojiLayout = new StaticLayout(
                emoji,
                emojiPaint,
                maxTextWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                false
        );
        totalWidth += bounds.width();

        countPaint.getTextBounds(count.toString(), 0, count.length(), bounds);
        reactionCountLayout = new StaticLayout(
                count,
                countPaint,
                maxTextWidth,
                Layout.Alignment.ALIGN_NORMAL,
                1.0f,
                0.0f,
                false
        );
        totalWidth += bounds.width();
        totalWidth += paddingBetweenEmoji;
        totalWidth += paddingLeftRight * 2;
        buttonWidth = totalWidth;
        totalWidth += margin;
    }

    public void createAvatarsOffline(View view, int currentAccount, String reaction, List<TLRPC.TL_messageUserReaction> recentReactons, ArrayList<TLRPC.User> users) {
        List<TLRPC.TL_messageUserReaction> reactedUsers = new ArrayList<>();
        for (TLRPC.TL_messageUserReaction reactedUser: recentReactons) {
            if (reactedUser.reaction.equals(reaction)) {
                reactedUsers.add(reactedUser);
            }
        }

        int size = reactedUsers.size();
        reactionAvatarImages = new ImageReceiver[size];
        reactionAvatarDrawables = new AvatarDrawable[size];
        for (int a = 0; a < size; a++) {
            reactionAvatarImages[a] = new ImageReceiver(view);
            reactionAvatarImages[a].setRoundRadius(reactedUserAvatarSize / 2);
            reactionAvatarDrawables[a] = new AvatarDrawable();
            reactionAvatarDrawables[a].setTextSize(AndroidUtilities.dp(12));
        }

        for (int a = 0; a < reactionAvatarImages.length; a++) {
            TLRPC.TL_messageUserReaction reactedUser = reactedUsers.get(a);
            if (a < size) {
                reactionAvatarImages[a].setImageCoords(0, 0, reactedUserAvatarSize - reactedUserAvatarPadding * 2, reactedUserAvatarSize - reactedUserAvatarPadding * 2);
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(reactedUser.user_id);
                if (user != null) {
                    reactionAvatarDrawables[a].setInfo(user);
                    reactionAvatarImages[a].setForUserOrChat(user, reactionAvatarDrawables[a]);
                } else if (users != null ){
                    for (TLRPC.User user1: users) {
                        if (user1.id == reactedUser.user_id) {
                            reactionAvatarDrawables[a].setInfo(user1);
                            reactionAvatarImages[a].setForUserOrChat(user1, reactionAvatarDrawables[a]);
                            break;
                        }
                    }
                } else {
                    reactionAvatarDrawables[a].setInfo(reactedUser.user_id, "", "");
                }
            }
        }
    }

    public void createAvatarsOnline(View view, ArrayList<TLRPC.TL_messageUserReaction> reactions, ArrayList<TLRPC.User> users) {
        ArrayList<TLRPC.User> selectedUsers = new ArrayList<>();
        for (TLRPC.TL_messageUserReaction reaction: reactions) {
            if (reaction.reaction.equals(getReaction())) {
                for (TLRPC.User user: users) {
                    if (user.id == reaction.user_id) {
                        selectedUsers.add(user);
                    }
                }
            }
        }

        int size = Math.min(reactionCount.count, selectedUsers.size());
        if (reactionAvatarImages != null && reactionAvatarImages.length == size) {
            return;
        }

        reactionAvatarImages = new ImageReceiver[size];
        reactionAvatarDrawables = new AvatarDrawable[size];
        for (int a = 0; a < size; a++) {
            reactionAvatarImages[a] = new ImageReceiver(view);
            reactionAvatarImages[a].setRoundRadius(reactedUserAvatarSize / 2);
            reactionAvatarDrawables[a] = new AvatarDrawable();
            reactionAvatarDrawables[a].setTextSize(AndroidUtilities.dp(12));
        }

        for (int a = 0; a < size; a++) {
            TLRPC.User reactedUser = selectedUsers.get(a);
            if (a < size) {
                reactionAvatarImages[a].setImageCoords(0, 0, reactedUserAvatarSize - reactedUserAvatarPadding * 2, reactedUserAvatarSize - reactedUserAvatarPadding * 2);
                if (reactedUser != null) {
                    reactionAvatarDrawables[a].setInfo(reactedUser);
                    reactionAvatarImages[a].setForUserOrChat(reactedUser, reactionAvatarDrawables[a]);
                } else {
                    reactionAvatarDrawables[a].setInfo(reactedUser.id, "", "");
                }
            }
        }
    }

    public void draw(Canvas canvas) {
        int emojiDy = (buttonHeight - reactionEmojiLayout.getLineBottom(reactionEmojiLayout.getLineCount() - 1)) / 2;
        int countDy = (buttonHeight - reactionCountLayout.getLineBottom(reactionCountLayout.getLineCount() - 1)) / 2;
        canvas.translate(paddingLeftRight, emojiDy);
        reactionEmojiLayout.draw(canvas);
        canvas.translate(emojiSize + paddingBetweenEmoji, -emojiDy + countDy);
        reactionCountLayout.draw(canvas);

        if (reactionAvatarImages != null) {
            int avatarDy = (buttonHeight - reactedUserAvatarSize) / 2;
            canvas.translate(0, -countDy + avatarDy);
            for (int a = Math.min(getReactionCount().count - 1, reactionAvatarImages.length - 1); a >= 0; a--) {
                if (!reactionAvatarImages[a].hasImageSet()) {
                    continue;
                }
                reactionAvatarImages[a].setImageX(reactedUserAvatarPadding + reactedUserAvatarShift * a);
                reactionAvatarImages[a].setImageY(reactedUserAvatarPadding);
                if (a != reactionAvatarImages.length - 1) {
                    canvas.drawCircle(reactionAvatarImages[a].getCenterX(), reactionAvatarImages[a].getCenterY(), reactedUserAvatarSize / 2f, backgroundPaint);
                }
                reactionAvatarImages[a].draw(canvas);
            }
        }
    }

    public void setRect(RectF rect) {
        this.rect = new RectF(rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean containts(int x, int y) {
        return rect != null && rect.left <= x && x <= rect.right && rect.top <= y && y <= rect.bottom;
    }

    public String getReaction() {
        return reactionCount.reaction;
    }

    public TLRPC.TL_reactionCount getReactionCount() {
        return reactionCount;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void onAttachedToWindow() {
        if (reactionAvatarImages != null) {
            for (int a = 0; a < reactionAvatarImages.length; a++) {
                reactionAvatarImages[a].onAttachedToWindow();
            }
        }
    }

    public void onDetachedFromWindow() {
        if (reactionAvatarImages != null) {
            for (int a = 0; a < reactionAvatarImages.length; a++) {
                reactionAvatarImages[a].onDetachedFromWindow();
            }
        }
    }
}
