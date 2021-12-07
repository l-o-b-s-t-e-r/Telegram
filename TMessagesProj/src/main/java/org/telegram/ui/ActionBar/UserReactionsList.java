package org.telegram.ui.ActionBar;

import android.content.Context;
import android.util.Pair;
import android.view.ViewGroup;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.MessageObject;
import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;
import java.util.List;

public class UserReactionsList extends RecyclerListView {

    boolean loading = false;
    private int visibleThreshold = 5;
    String currentPage = "";
    String reaction = "";

    public List<Pair<TLRPC.User, String>> items = new ArrayList<>();
    LinearLayoutManager layoutManager;
    MessageObject messageObject;
    TLRPC.InputPeer peer;
    ConnectionsManager connectionsManager;
    SelectionAdapter adapter;
    OnScrollListener onScrollListener;
    int requestToken = 0;

    public UserReactionsList(Context context) {
        super(context);
    }

    public UserReactionsList(Context context, Theme.ResourcesProvider resourcesProvider) {
        super(context, resourcesProvider);
    }

    public UserReactionsList(Context context, MessageObject msg, String reaction, int currentAccount, List<Pair<TLRPC.User, String>> initItems, String currentIndex) {
        super(context);
        layoutManager = new LinearLayoutManager(context);
        peer = MessagesController.getInstance(currentAccount).getInputPeer(msg.getDialogId());
        connectionsManager = ConnectionsManager.getInstance(currentAccount);
        messageObject = msg;
        this.reaction = reaction;
        this.currentPage = currentIndex;
        setLayoutManager(layoutManager);

        if (initItems != null) {
            items.addAll(initItems);
        }

        setAdapter(adapter = new RecyclerListView.SelectionAdapter() {
            @Override
            public boolean isEnabled(RecyclerView.ViewHolder holder) {
                return true;
            }

            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                ReactedUsersView.UserCell userCell = new ReactedUsersView.UserCell(parent.getContext());
                userCell.setLayoutParams(new RecyclerView.LayoutParams(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
                return new RecyclerListView.Holder(userCell);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ReactedUsersView.UserCell cell = (ReactedUsersView.UserCell) holder.itemView;
                Pair<TLRPC.User, String> pair = items.get(position);
                cell.setInfo(pair.first, pair.second);
            }

            @Override
            public int getItemCount() {
                return items.size();
            }

        });

        addScrollListener();
    }

    public void setReaction(String reaction) {
        connectionsManager.cancelRequest(requestToken, false);
        items.clear();
        adapter.notifyDataSetChanged();
        this.reaction = reaction;
        this.currentPage = "";
        addScrollListener();
    }

    public void initLoading() {
        TLRPC.TL_messages_getMessageReactionsList req = new TLRPC.TL_messages_getMessageReactionsList();
        req.id = messageObject.getId();
        req.peer = peer;
        req.limit = reaction.isEmpty() ? 100 : 50;
        req.flags |= 1;
        req.reaction = reaction;
        req.flags |= 2;
        req.offset = currentPage;

        requestToken = connectionsManager.sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
            if (error == null) {
                TLRPC.TL_messages_messageReactionsList res = (TLRPC.TL_messages_messageReactionsList) response;
                currentPage = res.next_offset;

                List<Long> ids = new ArrayList<>();
                List<Pair<TLRPC.User, String>> toAdd = new ArrayList<>();
                for (TLRPC.TL_messageUserReaction reaction: res.reactions) {
                    ids.add(reaction.user_id);
                    for (TLRPC.User user: res.users) {
                        if (reaction.user_id == user.id) {
                            toAdd.add(new Pair(user, reaction.reaction));
                            break;
                        }
                    }
                }
                int previousSize = items.size();
                items.addAll(toAdd);
                adapter.notifyItemRangeInserted(previousSize, toAdd.size());
                loading = false;
            } else {
                loading = false;
            }
        }));
    }

    private void addScrollListener() {
        removeOnScrollListener(onScrollListener);
        addOnScrollListener(onScrollListener = new EndlessScrollRecyclerListener() {
            @Override
            public void onLoadMore(String page) {
                TLRPC.TL_messages_getMessageReactionsList req = new TLRPC.TL_messages_getMessageReactionsList();
                req.id = messageObject.getId();
                req.peer = peer;
                req.limit = reaction.isEmpty() ? 100 : 50;
                req.flags |= 1;
                req.reaction = reaction;
                req.flags |= 2;
                req.offset = currentPage;

                requestToken = connectionsManager.sendRequest(req, (response, error) -> AndroidUtilities.runOnUIThread(() -> {
                    if (error == null) {
                        TLRPC.TL_messages_messageReactionsList res = (TLRPC.TL_messages_messageReactionsList) response;
                        currentPage = res.next_offset;

                        List<Long> ids = new ArrayList<>();
                        List<Pair<TLRPC.User, String>> toAdd = new ArrayList<>();
                        for (TLRPC.TL_messageUserReaction reaction: res.reactions) {
                            ids.add(reaction.user_id);
                            for (TLRPC.User user: res.users) {
                                if (reaction.user_id == user.id) {
                                    toAdd.add(new Pair(user, reaction.reaction));
                                    break;
                                }
                            }
                        }
                        int previousSize = items.size();
                        items.addAll(toAdd);
                        adapter.notifyItemRangeInserted(previousSize, toAdd.size());
                        loading = false;
                    } else {
                        loading = false;
                    }
                }));
            }
        });
    }

    public abstract class EndlessScrollRecyclerListener extends RecyclerView.OnScrollListener {

        @Override
        public void onScrolled(RecyclerView mRecyclerView, int dx, int dy) {
            super.onScrolled(mRecyclerView, dx, dy);
            onScroll(layoutManager.findFirstVisibleItemPosition(), mRecyclerView.getChildCount(), layoutManager.getItemCount());
        }

        public void onScroll(int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (!loading && currentPage != null) {
                if ((totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold) || items.isEmpty()) {
                    loading = true;
                    onLoadMore(currentPage);
                }
            }
        }

        public abstract void onLoadMore(String page);

    }
}
