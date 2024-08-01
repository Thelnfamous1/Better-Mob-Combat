package me.Thelnfamous1.bettermobcombat.logic;

import me.Thelnfamous1.bettermobcombat.BetterMobCombat;
import net.bettercombat.logic.TargetHelper;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Monster;

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
                if(attacker.getTeam() != null){
                    if(attacker.getTeam().isAlliedTo(target.getTeam())) {
                        return TargetHelper.Relation.FRIENDLY;
                    } else if(BetterMobCombat.getServerConfig().team_mobs_only_respect_teams){
                        return TargetHelper.Relation.HOSTILE;
                    }
                }
                // continue to the rest of the checks
                if(BetterMobCombat.getServerConfig().mobs_check_for_vanilla_allies && attacker.isAlliedTo(target)){
                    return TargetHelper.Relation.FRIENDLY;
                } else if(BetterMobCombat.getServerConfig().mobs_check_for_same_entity_type && attacker.getType().equals(target.getType())){
                    return TargetHelper.Relation.FRIENDLY;
                } else if(BetterMobCombat.getServerConfig().mobs_check_for_same_mob_type
                        && target instanceof LivingEntity livingTarget && attacker.getMobType().equals(livingTarget.getMobType())){
                    return TargetHelper.Relation.FRIENDLY;
                } else {
                    TargetHelper.Relation relationToTarget = BetterMobCombat.getServerConfigHelper().getMobRelation(attacker.getType(), target.getType());
                    if (relationToTarget != null) {
                        return relationToTarget;
                    } else if (target instanceof AgeableMob) {
                        return TargetHelper.Relation.coalesce(BetterMobCombat.getServerConfigHelper().getMobRelationToPassives(attacker.getType()), TargetHelper.Relation.HOSTILE);
                    } else {
                        return target instanceof Monster ?
                                TargetHelper.Relation.coalesce(BetterMobCombat.getServerConfigHelper().getMobRelationToHostiles(attacker.getType()), TargetHelper.Relation.HOSTILE) :
                                TargetHelper.Relation.coalesce(BetterMobCombat.getServerConfigHelper().getMobRelationToOther(attacker.getType()), TargetHelper.Relation.HOSTILE);
                    }
                }
            }
        }
    }
}