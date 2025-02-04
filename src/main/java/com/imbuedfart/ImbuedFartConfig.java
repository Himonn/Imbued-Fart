package com.imbuedfart;

import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.Range;

@ConfigGroup("imbuedfart")
public interface ImbuedFartConfig extends Config {

    @Range(
            min = 1,
            max = 10
    )
    @ConfigItem(
            keyName = "volumeLevel",
            name = "Volume",
            description = "Adjust fart volume",
            position = 8
    )
    default int volumeLevel() {
        return 10;
    }

    @ConfigItem(
            keyName = "fartMenuEntry",
            name = "Show Fart Menu Entry",
            description = "Adds a fart option to your imbued / saturated heart",
            position = 10
    )
    default boolean fartMenuEntry() { return false; }

    @ConfigItem(
            keyName = "menuEntryTrigger",
            name = "Trigger on Click",
            description = "Triggers on click, instead of sound effect, prevents rev spamming, prevents others",
            position = 10
    )
    default boolean menuEntryTrigger() { return false; }
}
