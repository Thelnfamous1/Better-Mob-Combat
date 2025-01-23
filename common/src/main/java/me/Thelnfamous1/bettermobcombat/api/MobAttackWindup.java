package me.Thelnfamous1.bettermobcombat.api;

import net.bettercombat.api.WeaponAttributes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public interface MobAttackWindup {

    default void bettermobcombat$startUpswing(WeaponAttributes attributes){
        this.bettermobcombat$startUpswing(attributes, null);
    }

    void bettermobcombat$startUpswing(WeaponAttributes attributes, @Nullable BiConsumer<Mob, Entity> customDamageApplicator);

    int bettermobcombat$getUpswingTicks();

    int bettermobcombat$getAttackCooldown();

    float bettermobcombat$getSwingProgress();

    default boolean isWeaponSwingInProgress() {
        return this.bettermobcombat$getSwingProgress() < 1.0F;
    }

    void bettermobcombat$cancelUpswing();

    void bettermobcombat$setDelayedUpswing(Runnable runnable);

    boolean bettermobcombat$hasDelayedUpswing();
}
