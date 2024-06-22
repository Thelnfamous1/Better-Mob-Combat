package me.Thelnfamous1.bettermobcombat.logic;

import net.bettercombat.BetterCombat;
import net.bettercombat.config.ServerConfig;
import net.bettercombat.logic.TargetHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.scores.Team;

public class MobTargetHelper {
    public MobTargetHelper() {
    }

    public static TargetHelper.Relation getRelation(LivingEntity attacker, Entity target) {
        if (attacker == target) {
            return TargetHelper.Relation.FRIENDLY;
        } else {
            if (target instanceof OwnableEntity) {
                OwnableEntity tameable = (OwnableEntity)target;
                LivingEntity owner = tameable.getOwner();
                if (owner != null) {
                    return getRelation(attacker, owner);
                }
            }

            if (target instanceof HangingEntity) {
                return TargetHelper.Relation.NEUTRAL;
            } else {
                ServerConfig config = BetterCombat.config;
                Team casterTeam = attacker.getTeam();
                Team targetTeam = target.getTeam();
                if (casterTeam != null && targetTeam != null) {
                    return attacker.isAlliedTo(target) ? TargetHelper.Relation.FRIENDLY : TargetHelper.Relation.HOSTILE;
                } else {
                    ResourceLocation id = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
                    TargetHelper.Relation mappedRelation = (TargetHelper.Relation)config.player_relations.get(id.toString());
                    if (mappedRelation != null) {
                        return mappedRelation;
                    } else if (target instanceof AgeableMob) {
                        return TargetHelper.Relation.coalesce(config.player_relation_to_passives, TargetHelper.Relation.HOSTILE);
                    } else {
                        return target instanceof Monster ? TargetHelper.Relation.coalesce(config.player_relation_to_hostiles, TargetHelper.Relation.HOSTILE) : TargetHelper.Relation.coalesce(config.player_relation_to_other, TargetHelper.Relation.HOSTILE);
                    }
                }
            }
        }
    }
}