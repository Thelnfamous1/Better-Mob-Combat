package me.Thelnfamous1.bettermobcombat.compatibility;

import net.bettercombat.Platform;
import net.bettercombat.compatibility.PehkuiHelper;

public class BMCCompatibilityFlags {

    public static void initialize() {
        if (Platform.isModLoaded("pehkui")) {
            PehkuiHelper.load();
        }
    }
}