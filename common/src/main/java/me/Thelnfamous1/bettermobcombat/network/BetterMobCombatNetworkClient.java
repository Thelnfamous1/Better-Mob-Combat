package me.Thelnfamous1.bettermobcombat.network;

import net.bettercombat.client.animation.PlayerAttackAnimatable;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.network.Packets;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public class BetterMobCombatNetworkClient {
    static void handleAttackAnimation(int mobId, String animationName, float length, AnimatedHand animatedHand, float upswing) {
        Entity entity = Minecraft.getInstance().level.getEntity(mobId);
        if (entity instanceof LivingEntity) {
            if (animationName.equals(Packets.AttackAnimation.StopSymbol)) {
                ((PlayerAttackAnimatable)entity).stopAttackAnimation(length);
            } else {
                ((PlayerAttackAnimatable)entity).playAttackAnimation(animationName, animatedHand, length, upswing);
            }
        }
    }
}
