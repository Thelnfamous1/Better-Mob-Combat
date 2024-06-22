package me.Thelnfamous1.bettermobcombat.duck;

import net.bettercombat.api.WeaponAttributes;

public interface MobAttackWindup {

    void bettermobcombat$startUpswing(WeaponAttributes attributes);

    int bettermobcombat$getUpswingTicks();

    float bettermobcombat$getSwingProgress();

    default boolean isWeaponSwingInProgress() {
        return this.bettermobcombat$getSwingProgress() < 1.0F;
    }

    void bettermobcombat$cancelUpswing();
}
