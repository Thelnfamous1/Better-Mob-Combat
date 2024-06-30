package me.Thelnfamous1.bettermobcombat.compatibility;

import net.bettercombat.Platform;

public class BMCCompatibilityFlags {

    public static void initialize() {
        if (Platform.isModLoaded("pehkui")) {
            BMCPehkuiHelper.load();
        }
    }
}