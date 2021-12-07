package org.telegram.ui.ActionBar;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ChatObject;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.AvatarsImageView;
import org.telegram.ui.Components.FlickerLoadingView;
import org.telegram.ui.Components.HideViewAfterAnimation;
import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ReactionView extends FrameLayout {

    private AvatarsImageView avatarsImageView;
    private TextView reactionView;
    private TextView countView;
    private FlickerLoadingView flickerLoadingView;

    private int currentAccount;
    private boolean isChecked;
    private boolean displayReactedUsers;
    private ReactionViewColors colorsConfiguration;

    private ArrayList<Long> peerIds = new ArrayList<>();
    private ArrayList<TLRPC.User> users = new ArrayList<>();

    public ReactionView(@NonNull Context context, int currentAccount, MessageObject messageObject, TLRPC.Chat chat, TLRPC.TL_reactionCount reactionCount, List<Long> reactedUsersIds, ReactionViewColors colorsConfiguration) {
        super(context);
        this.currentAccount = currentAccount;
        this.colorsConfiguration = colorsConfiguration;
        displayReactedUsers = !ChatObject.isChannel(chat) && reactionCount.count < 4;

        reactionView = new TextView(context);
        reactionView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        reactionView.setTextColor(Theme.getColor(Theme.key_chat_reactionText));
        addView(reactionView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL, 4, 0, 4, 0));

        if (displayReactedUsers) {
            flickerLoadingView = new FlickerLoadingView(context);
            flickerLoadingView.setColors(Theme.key_actionBarDefaultSubmenuBackground, Theme.key_listSelector, null);
            flickerLoadingView.setViewType(FlickerLoadingView.MESSAGE_SEEN_TYPE);
            flickerLoadingView.setIsSingleCell(false);
            addView(flickerLoadingView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.CENTER_VERTICAL));
        }

        countView = new TextView(context);
        countView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        countView.setTextColor(colorsConfiguration.getTextColor());
        countView.setText(String.valueOf(reactionCount.count));
        addView(countView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.LEFT | Gravity.CENTER_VERTICAL, 24, 0, 4, 0));

        if (displayReactedUsers) {
            avatarsImageView = new AvatarsImageView(context, false);
            avatarsImageView.setStyle(AvatarsImageView.STYLE_MESSAGE_SEEN);
            addView(avatarsImageView, LayoutHelper.createFrame(24 + 12 + 12 + 8, LayoutHelper.MATCH_PARENT, Gravity.RIGHT | Gravity.CENTER_VERTICAL));
            avatarsImageView.setAlpha(0);

            long fromId = 0;
            if (messageObject.messageOwner.from_id != null) {
                fromId = messageObject.messageOwner.from_id.user_id;
            }
            long finalFromId = fromId;

            ArrayList<Long> unknownUsers = new ArrayList<>();
            HashMap<Long, TLRPC.User> usersLocal = new HashMap<>();
            ArrayList<Long> allPeers = new ArrayList<>();
            for (int i = 0, n = reactedUsersIds.size(); i < n; i++) {
                Long peerId = reactedUsersIds.get(i);
                if (finalFromId == peerId) {
                    continue;
                }
                TLRPC.User user = MessagesController.getInstance(currentAccount).getUser(peerId);
                allPeers.add(peerId);
                if (user == null) {
                    unknownUsers.add(peerId);
                } else {
                    usersLocal.put(peerId, user);
                }
            }

            if (unknownUsers.isEmpty()) {
                for (int i = 0; i < allPeers.size(); i++) {
                    peerIds.add(allPeers.get(i));
                    users.add(usersLocal.get(allPeers.get(i)));
                }
                updateView();
            } else {
                TLRPC.TL_messages_getFullChat usersReq = new TLRPC.TL_messages_getFullChat();
                usersReq.chat_id = chat.id;
                ConnectionsManager.getInstance(currentAccount).sendRequest(usersReq, (response1, error1) -> AndroidUtilities.runOnUIThread(() -> {
                    if (response1 != null) {
                        TLRPC.TL_messages_chatFull chatFull = (TLRPC.TL_messages_chatFull) response1;
                        for (int i = 0; i < chatFull.users.size(); i++) {
                            TLRPC.User user = chatFull.users.get(i);
                            MessagesController.getInstance(currentAccount).putUser(user, false);
                            usersLocal.put(user.id, user);
                        }
                        for (int i = 0; i < allPeers.size(); i++) {
                            peerIds.add(allPeers.get(i));
                            this.users.add(usersLocal.get(allPeers.get(i)));
                        }
                    }
                    updateView();
                }));
            }
        }

        updateBackground();
    }

    private void setIsChecked(boolean isChecked) {
        if (this.isChecked != isChecked) {
            this.isChecked = isChecked;
            updateBackground();
        }
    }

    private void updateBackground() {
        setBackground(Theme.createRadSelectorDrawable(
                colorsConfiguration.getBackgroundColor(),
                colorsConfiguration.getBorderColor(),
                Theme.getColor(Theme.key_dialogButtonSelector),
                AndroidUtilities.dp(isChecked ? 2 : 0),
                AndroidUtilities.dp(8),
                AndroidUtilities.dp(8))
        );
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
        if (flickerLoadingView != null && flickerLoadingView.getVisibility() == View.VISIBLE) {
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
        setEnabled(users.size() > 0);
        if (avatarsImageView != null) {
            for (int i = 0; i < 3; i++) {
                if (i < users.size()) {
                    avatarsImageView.setObject(i, currentAccount, users.get(i));
                } else {
                    avatarsImageView.setObject(i, currentAccount, null);
                }
            }
            if (users.size() == 1) {
                avatarsImageView.setTranslationX(AndroidUtilities.dp(24));
            } else if (users.size() == 2) {
                avatarsImageView.setTranslationX(AndroidUtilities.dp(12));
            } else {
                avatarsImageView.setTranslationX(0);
            }

            avatarsImageView.commitTransition(false);
        }

        if (avatarsImageView != null) {
            avatarsImageView.animate().alpha(1f).setDuration(220).start();
        }
        if (flickerLoadingView != null) {
            flickerLoadingView.animate().alpha(0f).setDuration(220).setListener(new HideViewAfterAnimation(flickerLoadingView)).start();
        }
    }
}
