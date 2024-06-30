package me.Thelnfamous1.bettermobcombat.compatibility;

import me.Thelnfamous1.bettermobcombat.api.MobAttackRangeExtensions;
import net.bettercombat.Platform;
import net.bettercombat.api.client.AttackRangeExtensions;
import net.bettercombat.api.client.AttackRangeExtensions.Operation;
import net.bettercombat.compatibility.PehkuiHelper;

public class BMCPehkuiHelper {

    public BMCPehkuiHelper() {
    }

    public static void load() {
    }

    static {
        if (Platform.isModLoaded("pehkui")) {
            try {
                MobAttackRangeExtensions.register((context) -> new AttackRangeExtensions.Modifier(PehkuiHelper.getScale(context.mob()), Operation.MULTIPLY));
            } catch (SecurityException | IllegalArgumentException ignored) {

            }
        }
    }
}