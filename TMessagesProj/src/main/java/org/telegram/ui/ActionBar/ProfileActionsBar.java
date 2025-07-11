package org.telegram.ui.ActionBar;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import org.telegram.ui.Components.LayoutHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;

public class ProfileActionsBar extends LinearLayout {

    private List<ProfileActionsBarItem> items = new ArrayList<>();

    public ActionBar.ActionBarMenuOnItemClick actionBarMenuOnItemClick;

    private final static int MAX_VISIBLE_ITEMS = 4;

    private boolean isDark;

    public ProfileActionsBar(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
    }

    public ProfileActionsBarItem addItem(int position, int priority, int id, int icon, CharSequence text) {
        ProfileActionsBarItem menuItem = new ProfileActionsBarItem(getContext());

        menuItem.setTag(id);
        menuItem.setText(text);
        menuItem.setIcon(icon);
        menuItem.setPosition(position);
        menuItem.setPriority(priority);
        menuItem.setIsDark(isDark);
        menuItem.setOnClickListener(view -> {
            onItemClick((Integer) view.getTag());
        });

        items.add(menuItem);

        return menuItem;
    }

    public void setIsLight(boolean isLight) {
        if (isDark == !isLight) {
            return;
        }

        this.isDark = !isLight;
        for (ProfileActionsBarItem item : items) {
            item.setIsDark(isDark);
        }
    }

    public void removeItem(int position) {
        items.removeIf(item -> position == item.getPosition());
    }

    public ProfileActionsBarItem addItem(int position, int id, int icon, CharSequence text) {
        return addItem(position, 0, id, icon, text);
    }

    public void attachItemsToView() {
        Collections.sort(items, Comparator.comparingInt(ProfileActionsBarItem::getPosition).thenComparing(ProfileActionsBarItem::getPriority));

        int visibleItems = 0;
        for (int i = 0; i < items.size(); i++) {
            ProfileActionsBarItem item = items.get(i);
            int itemVisibility = item.getVisibility();

            if (itemVisibility == View.VISIBLE) {
                if (visibleItems >= MAX_VISIBLE_ITEMS || (i > 0 && items.get(i - 1).getPosition() == item.getPosition() && items.get(i - 1).getVisibility() == View.VISIBLE)) {
                    item.setVisibility(View.GONE);
                } else {
                    visibleItems++;
                }
            }

            addView(item, LayoutHelper.createLinear(0, LayoutHelper.WRAP_CONTENT, 1, Gravity.CENTER, 4, 0, 4, 0));
        }
    }

    public void removeAllItems() {
        items.clear();
        removeAllViews();
    }

    public void onItemClick(int id) {
        if (actionBarMenuOnItemClick != null) {
            actionBarMenuOnItemClick.onItemClick(id);
        }
    }

    public void setActionBarMenuOnItemClick(ActionBar.ActionBarMenuOnItemClick listener) {
        actionBarMenuOnItemClick = listener;
    }

    public void setScale(float scale) {
        for (ProfileActionsBarItem item : items) {
            item.setScale(scale);
        }
    }
}
