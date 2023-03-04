package org.telegram.ui.VoIP;

import androidx.core.graphics.ColorUtils;

public class BackgroundColors {
    public final int topLeft;
    public final int topRight;
    public final int bottomLeft;
    public final int bottomRight;

    public BackgroundColors(int topLeft, int topRight, int bottomLeft, int bottomRight) {
        this.topLeft = topLeft;
        this.topRight = topRight;
        this.bottomLeft = bottomLeft;
        this.bottomRight = bottomRight;
    }

    public BackgroundColors(BackgroundColors from, BackgroundColors to, float ratio) {
        topLeft = ColorUtils.blendARGB(from.topLeft, to.topLeft, ratio);
        topRight = ColorUtils.blendARGB(from.topRight, to.topRight, ratio);
        bottomLeft = ColorUtils.blendARGB(from.bottomLeft, to.bottomLeft, ratio);
        bottomRight = ColorUtils.blendARGB(from.bottomRight, to.bottomRight, ratio);
    }
}
