package org.telegram.ui.VoIP;

import org.telegram.ui.ActionBar.Theme;

public class BackgroundFactory {

    public static BackgroundColors getColors(BackgroundType type, boolean isDefault) {
        if (isDefault) {
            switch (type) {
                case blueViolet:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_blueVioletTopLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_blueVioletTopRightGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_blueVioletBottomLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_blueVioletBottomRightGradientDefault)
                    );
                case blueGreen:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_blueGreenTopLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_blueGreenTopRightGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_blueGreenBottomLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_blueGreenBottomRightGradientDefault)
                    );
                case green:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_greenTopLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_greenTopRightGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_greenBottomLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_greenBottomRightGradientDefault)
                    );
                case orangeRed:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_orangeRedTopLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_orangeRedTopRightGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_orangeRedBottomLeftGradientDefault),
                            Theme.getColor(Theme.key_voipgroup_orangeRedBottomRightGradientDefault)
                    );
            }
        } else {
            switch (type) {
                case blueViolet:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_blueVioletTopLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_blueVioletTopRightGradient),
                            Theme.getColor(Theme.key_voipgroup_blueVioletBottomLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_blueVioletBottomRightGradient)
                    );
                case blueGreen:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_blueGreenTopLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_blueGreenTopRightGradient),
                            Theme.getColor(Theme.key_voipgroup_blueGreenBottomLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_blueGreenBottomRightGradient)
                    );
                case green:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_greenTopLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_greenTopRightGradient),
                            Theme.getColor(Theme.key_voipgroup_greenBottomLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_greenBottomRightGradient)
                    );
                case orangeRed:
                    return new BackgroundColors(
                            Theme.getColor(Theme.key_voipgroup_orangeRedTopLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_orangeRedTopRightGradient),
                            Theme.getColor(Theme.key_voipgroup_orangeRedBottomLeftGradient),
                            Theme.getColor(Theme.key_voipgroup_orangeRedBottomRightGradient)
                    );
            }
        }

        throw new RuntimeException("No color pallet found");
    }

    public static BackgroundColors getDarkColors(BackgroundType type) {
        switch (type) {
            case blueViolet:
                return new BackgroundColors(
                        Theme.getColor(Theme.key_voipgroup_blueVioletDarkTopLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_blueVioletDarkTopRightGradient),
                        Theme.getColor(Theme.key_voipgroup_blueVioletDarkBottomLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_blueVioletDarkBottomRightGradient)
                );
            case blueGreen:
                return new BackgroundColors(
                        Theme.getColor(Theme.key_voipgroup_blueGreenDarkTopLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_blueGreenDarkTopRightGradient),
                        Theme.getColor(Theme.key_voipgroup_blueGreenDarkBottomLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_blueGreenDarkBottomRightGradient)
                );
            case green:
                return new BackgroundColors(
                        Theme.getColor(Theme.key_voipgroup_greenDarkTopLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_greenDarkTopRightGradient),
                        Theme.getColor(Theme.key_voipgroup_greenDarkBottomLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_greenDarkBottomRightGradient)
                );
            case orangeRed:
                return new BackgroundColors(
                        Theme.getColor(Theme.key_voipgroup_orangeRedDarkTopLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_orangeRedDarkTopRightGradient),
                        Theme.getColor(Theme.key_voipgroup_orangeRedDarkBottomLeftGradient),
                        Theme.getColor(Theme.key_voipgroup_orangeRedDarkBottomRightGradient)
                );
        }

        throw new RuntimeException("No color pallet found");
    }
}
