package me.Thelnfamous1.bettermobcombat.logic;

import net.bettercombat.logic.TargetHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.HangingEntity;

public class MobTargetHelper {
    public MobTargetHelper() {
    }

    public static TargetHelper.Relation getRelation(LivingEntity attacker, Entity target) {
        if (attacker == target) {
            return TargetHelper.Relation.FRIENDLY;
        } else {
            if (target instanceof OwnableEntity tameable) {
                LivingEntity owner = tameable.getOwner();
                if (owner != null) {
                    return getRelation(attacker, owner);
                }
            }

            if (target instanceof HangingEntity) {
                return TargetHelper.Relation.NEUTRAL;
            } else {
                return attacker.isAlliedTo(target) ? TargetHelper.Relation.FRIENDLY : TargetHelper.Relation.HOSTILE;
            }
        }
    }
}