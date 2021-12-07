package org.telegram.ui.ActionBar;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Pair;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ContactsController;
import org.telegram.messenger.DocumentObject;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.ImageLocation;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.SvgHelper;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.AvatarsImageView;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.HideViewAfterAnimation;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReactedUsersView extends FrameLayout {

    public Map<TLRPC.User, String> recentUsers = new LinkedHashMap<>();
    public List<Pair<TLRPC.User, String>> allUsers = new ArrayList<>();
    public Map<String, List<TLRPC.User>> allReactions = new LinkedHashMap<>();
    public List<TLRPC.TL_messageUserReaction> recentReactions = new ArrayList<>();
    public List<TLRPC.TL_reactionCount> countResults = new ArrayList<>();
    AvatarsImageView avatarsImageView;
    TextView titleView;
    TextView reactionView;
    ImageView iconView;
    int currentAccount;
    public String offset;

    FlickerLoadingView flickerLoadingView;

    private TLRPC.Chat chat;
    private int totalReactionsCount = 0;
    final long finalFromId;

    public ReactedUsersView(@NonNull Context context, int currentAccount, MessageObject messageObject, TLRPC.Chat chat) {
        super(context);
        this.currentAccount = currentAccount;
        this.chat = chat;
        long fromId = 0;

        if (messageObject.messageOwner.reactions != null) {
            countResults.addAll(messageObject.messageOwner.reactions.results);
        }

        if (messageObject.messageOwner.from_id != null) {
            fromId = messageObject.messageOwner.from_id.user_id;
        }
        finalFromId = fromId;

        flickerLoadingView = new FlickerLoadingView(context);
        flickerLoadingView.setColors(Theme.key_actionBarDefaultSubmenuBackground, Theme.key_listSelector, null);
        flickerLoadingView.setViewType(FlickerLoadingView.MESSAGE_SEEN_TYPE);
        flickerLoadingView.setIsSingleCell(false);
        addView(flickerLoadingView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT));

        titleView = new TextView(context);
        titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        titleView.setLines(1);
        titleView.setEllipsize(TextUtils.TruncateAt.END);

        addView(titleView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 40, 0, 62, 0));

        avatarsImageView = new AvatarsImageView(context, false);
        avatarsImageView.setStyle(AvatarsImageView.STYLE_MESSAGE_SEEN);
        addView(avatarsImageView, LayoutHelper.createFrame(24 + 12 + 12 + 8, LayoutHelper.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 0, 0));

        titleView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));

        TLRPC.TL_messages_getMessageReactionsList req = new TLRPC.TL_messages_getMessageReactionsList();
        req.id = messageObject.getId();
        req.peer = MessagesController.getInstance(currentAccount).getInputPeer(messageObject.getDialogId());
        req.limit = 100;

        iconView = new ImageView(context);
        addView(iconView, LayoutHelper.createFrame(24, 24, Gravity.LEFT | Gravity.CENTER_VERTICAL, 11, 0, 0, 0));
        Drawable drawable = ContextCompat.getDrawable(context, R.drawable.actions_reactions).mutate();
        drawable.setColorFilter(new PorterDuffColorFilter(Theme.getColor(Theme.key_actionBarDefaultSubmenuItemIcon), PorterDuff.Mode.MULTIPLY));
        iconView.setImageDrawable(drawable);

        reactionView = new TextView(context);
        reactionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
        addView(reactionView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 11, 0, 0, 0));
        reactionView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));

        avatarsImageView.setAlpha(0);
        titleView.setAlpha(0);
        ConnectionsManager.getInstance(currentAccount).sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_messages_messageReactionsList res = (TLRPC.TL_messages_messageReactionsList) response;
                totalReactionsCount = res.count;
                offset = res.next_offset;

                TLRPC.TL_reactionCount reactionCount = new TLRPC.TL_reactionCount();
                reactionCount.reaction = "";
                reactionCount.count = totalReactionsCount;
                countResults.add(0, reactionCount);
                processReactedUsers(res.reactions, res.users);
            } else {
                updateView();
            }
        }));
        setBackground(Theme.createRadSelectorDrawable(Theme.getColor(Theme.key_dialogButtonSelector), AndroidUtilities.dp(4), AndroidUtilities.dp(4)));
        setEnabled(false);
    }

    private void processReactedUsers(List<TLRPC.TL_messageUserReaction> recentReactions, List<TLRPC.User> reactedUsers) {
        if (recentReactions != null) {
            this.recentReactions = recentReactions;
            for (int i = 0, n = recentReactions.size(); i < n; i++) {
                TLRPC.TL_messageUserReaction reactedUser = recentReactions.get(i);

                List<TLRPC.User> allSpecificReactions = allReactions.get(reactedUser.reaction);
                if (allSpecificReactions == null) {
                    allSpecificReactions = new ArrayList<>();
                }

                for (TLRPC.User user: reactedUsers) {
                    if (user.id == reactedUser.user_id) {
                        allUsers.add(new Pair(user, reactedUser.reaction));
                        allSpecificReactions.add(user);
                        if (recentUsers.size() < 3) {
                            recentUsers.put(user, reactedUser.reaction);
                        }
                        break;
                    }
                }

                allReactions.put(reactedUser.reaction, allSpecificReactions);
            }
            updateView();
        }
    }

    boolean ignoreLayout;

    @Override
    public void requestLayout() {
        if (ignoreLayout) {
            return;
        }
        super.requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (flickerLoadingView.getVisibility() == View.VISIBLE) {
            ignoreLayout = true;
            flickerLoadingView.setVisibility(View.GONE);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            flickerLoadingView.getLayoutParams().width = getMeasuredWidth();
            flickerLoadingView.setVisibility(View.VISIBLE);
            ignoreLayout = false;
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    private void updateView() {
        setEnabled(recentUsers.size() > 0);
        List<TLRPC.User> recentUsersList = new ArrayList<>(recentUsers.keySet());
        for (int i = 0; i < 3; i++) {
            if (i < recentUsers.size()) {
                avatarsImageView.setObject(i, currentAccount, recentUsersList.get(i));
            } else {
                avatarsImageView.setObject(i, currentAccount, null);
            }
        }
        if (recentUsers.size() == 1) {
            avatarsImageView.setTranslationX(AndroidUtilities.dp(24));
        } else if (recentUsers.size() == 2) {
            avatarsImageView.setTranslationX(AndroidUtilities.dp(12));
        } else {
            avatarsImageView.setTranslationX(0);
        }

        avatarsImageView.commitTransition(false);
        if (recentUsersList.size() == 1 && recentUsersList.get(0) != null && recentUsers.get(recentUsersList.get(0)) != null) {
            String reaction = recentUsers.get(recentUsersList.get(0));
            iconView.setVisibility(GONE);
            reactionView.setVisibility(VISIBLE);
            reactionView.setText(reaction);
            titleView.setText(ContactsController.formatName(recentUsersList.get(0).first_name, recentUsersList.get(0).last_name));
        } else {
            iconView.setVisibility(VISIBLE);
            reactionView.setVisibility(GONE);
            titleView.setText(String.format("%s %s", AndroidUtilities.formatWholeNumber(totalReactionsCount, 0), LocaleController.getString("Reactions", R.string.Reactions)));
        }
        titleView.animate().alpha(1f).setDuration(220).start();
        avatarsImageView.animate().alpha(1f).setDuration(220).start();
        flickerLoadingView.animate().alpha(0f).setDuration(220).setListener(new HideViewAfterAnimation(flickerLoadingView)).start();
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

    public static class UserCell extends FrameLayout {

        BackupImageView avatarImageView;
        TextView nameView;
        TextView reactionView;
        AvatarDrawable avatarDrawable = new AvatarDrawable();

        public UserCell(Context context) {
            super(context);
            reactionView = new TextView(context);
            reactionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
            addView(reactionView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL, 0, 0, 11, 0));
            reactionView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));

            avatarImageView = new BackupImageView(context);
            addView(avatarImageView, LayoutHelper.createFrame(32, 32, Gravity.CENTER_VERTICAL, 13, 0, 0, 0));
            avatarImageView.setRoundRadius(AndroidUtilities.dp(16));

            nameView = new TextView(context);
            nameView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            nameView.setLines(1);
            nameView.setEllipsize(TextUtils.TruncateAt.END);
            addView(nameView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 59, 0, 18 + 20, 0));

            nameView.setTextColor(Theme.getColor(Theme.key_actionBarDefaultSubmenuItem));
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(44), View.MeasureSpec.EXACTLY));
        }

        public void setInfo(TLRPC.User user, String reaction) {
            if (user != null) {
                reactionView.setText(reaction);
                avatarDrawable.setInfo(user);
                ImageLocation imageLocation = ImageLocation.getForUser(user, ImageLocation.TYPE_SMALL);
                avatarImageView.setImage(imageLocation, "50_50", avatarDrawable, user);
                nameView.setText(ContactsController.formatName(user.first_name, user.last_name));
            }
        }
    }
}
