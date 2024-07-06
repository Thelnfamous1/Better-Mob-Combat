package me.Thelnfamous1.bettermobcombat.compatibility;

import net.bettercombat.Platform;

public class BMCCompatibilityFlags {
    private static boolean geckolibLoaded;

    public static void initialize() {
        if (Platform.isModLoaded("pehkui")) {
            BMCPehkuiHelper.load();
        }
        if(Platform.isModLoaded("geckolib")){
            geckolibLoaded = true;
        }
    }

    public static boolean isGeckolibLoaded() {
        return geckolibLoaded;
    }
}