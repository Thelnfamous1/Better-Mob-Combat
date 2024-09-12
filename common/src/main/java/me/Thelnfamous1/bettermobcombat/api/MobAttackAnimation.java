package me.Thelnfamous1.bettermobcombat.api;

public interface MobAttackAnimation {
    boolean bettermobcombat$hasActiveAttackAnimation();

    boolean bettermobcombat$hasActiveMainHandItemPose();

    boolean bettermobcombat$hasActiveOffHandItemPose();

    boolean bettermobcombat$hasActiveMainHandBodyPose();

    boolean bettermobcombat$hasActiveOffHandBodyPose();

    default boolean bettermobcombat$isCombatAnimationActive(){
        return this.bettermobcombat$hasActiveAttackAnimation()
                || this.bettermobcombat$hasActiveMainHandItemPose()
                || this.bettermobcombat$hasActiveOffHandItemPose()
                || this.bettermobcombat$hasActiveMainHandBodyPose()
                || this.bettermobcombat$hasActiveOffHandBodyPose();
    }
}
