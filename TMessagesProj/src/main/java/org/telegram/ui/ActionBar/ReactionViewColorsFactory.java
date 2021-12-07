package org.telegram.ui.ActionBar;

public class ReactionViewColorsFactory {
    public int GREEN_BACKGROUND = 0;
    public int WHITE_BACKGROUND = 1;
    public int TRANSPARENT_BACKGROUND = 2;

    ReactionViewColors getColors(int messageBackgroundType, Theme.ResourcesProvider resourcesProvider) {
        ReactionViewColors colors = null;
        switch (messageBackgroundType) {
            case 0:
                colors = new ReactionViewColors() {
                    @Override
                    public int getBackgroundColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }

                    @Override
                    public int getBorderColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }

                    @Override
                    public int getTextColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }
                };
                break;
            case 1:
                colors = new ReactionViewColors() {
                    @Override
                    public int getBackgroundColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }

                    @Override
                    public int getBorderColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }

                    @Override
                    public int getTextColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }
                };
                break;
            case 2:
                colors = new ReactionViewColors() {
                    @Override
                    public int getBackgroundColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }

                    @Override
                    public int getBorderColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }

                    @Override
                    public int getTextColor() {
                        return getThemedColor(Theme.key_windowBackgroundWhite, resourcesProvider);
                    }
                };
                break;
        }

        return colors;
    }

    private int getThemedColor(String key, Theme.ResourcesProvider resourcesProvider) {
        Integer color = resourcesProvider != null ? resourcesProvider.getColor(key) : null;
        return color != null ? color : Theme.getColor(key);
    }
}
