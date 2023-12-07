package org.telegram.ui.Components.spoilers;

import android.graphics.Bitmap;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;

import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;
import java.util.List;

public class DisintegrationEffects {

    private final List<DisintegrationEffect> effects;

    public static boolean supports() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public DisintegrationEffects() {
        effects = new ArrayList<>();
    }

    public void onDetachedFromWindow() {
        for (int i = 0; i < effects.size(); i++) {
            effects.get(i).destroy();
        }
        effects.clear();
    }

    public void addNewEffect(View view, Bitmap bitmap, float top, int left) {
        final int size = getSize();
        final DisintegrationEffect effect = new DisintegrationEffect((ViewGroup) view.getParent(), bitmap, size, size, top - size, left);
        effects.add(effect);
    }

    private int getSize() {
        return Math.min(AndroidUtilities.displaySize.x, AndroidUtilities.displaySize.y);
    }
}
