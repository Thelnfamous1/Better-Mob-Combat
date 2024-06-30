package me.Thelnfamous1.bettermobcombat.api;

import net.bettercombat.api.WeaponAttributes;

public interface MobAttackWindup {

    void bettermobcombat$startUpswing(WeaponAttributes attributes);

    int bettermobcombat$getUpswingTicks();

    int bettermobcombat$getAttackCooldown();

    float bettermobcombat$getSwingProgress();

    default boolean isWeaponSwingInProgress() {
        return this.bettermobcombat$getSwingProgress() < 1.0F;
    }

    void bettermobcombat$cancelUpswing();
}
