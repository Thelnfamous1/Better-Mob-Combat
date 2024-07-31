package me.Thelnfamous1.bettermobcombat.config;

import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.compatibility.BMCCompatibilityFlags;
import me.Thelnfamous1.bettermobcombat.compatibility.GeckolibHelper;
import net.bettercombat.logic.TargetHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class BMCServerConfigHelper {
    private final BMCServerConfig serverConfig;
    private final Set<EntityType<?>> mobBlacklist = new HashSet<>();
    private final Map<EntityType<?>, Map<EntityType<?>, TargetHelper.Relation>> mobRelations = new HashMap<>();
    private final Map<EntityType<?>, TargetHelper.Relation> mobRelationsToPassives = new HashMap<>();
    private final Map<EntityType<?>, TargetHelper.Relation> mobRelationsToHostiles = new HashMap<>();
    private final Map<EntityType<?>, TargetHelper.Relation> mobRelationsToOther = new HashMap<>();

    public BMCServerConfigHelper(BMCServerConfig serverConfig, boolean log){
        this.serverConfig = serverConfig;
        // mob blacklist
        for(String entry : serverConfig.mob_blacklist){
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if(id == null){
                if(log) Constants.LOG.error("Could not parse mob blacklist entry {}, not a valid namespaced id", entry);
            } else{
                Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                entityType.ifPresentOrElse(et -> {
                    this.mobBlacklist.add(et);
                    if(log) Constants.LOG.debug("Entered {} into the mob blacklist!", id);
                }, () -> {
                    if(log) Constants.LOG.error("Could not find mob blacklist entry {}, not a valid entity type", id);
                });
            }
        }
        // mob relations
        for(Map.Entry<String, LinkedHashMap<String, TargetHelper.Relation>> entry : serverConfig.mob_relations.entrySet()){
            ResourceLocation id = ResourceLocation.tryParse(entry.getKey());
            if(id == null){
                if(log) Constants.LOG.error("Could not parse mob relations entry key {}, not a valid namespaced id", entry.getKey());
            } else{
                Optional<EntityType<?>> entityTypeKey = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                entityTypeKey.ifPresentOrElse(etk -> {
                    Map<String, TargetHelper.Relation> relations = entry.getValue();
                    parseMobRelations(relations, this.mobRelations.computeIfAbsent(etk, k -> new HashMap<>()), id + " mob_relations map", log);
                }, () -> {
                    if(log) Constants.LOG.error("Could not find mob relations entry key {}, not a valid entity type", id);
                });
            }
        }
        // mob relation to passives
        parseMobRelations(serverConfig.mob_relations_to_passives, this.mobRelationsToPassives, "mob_relations_to_passives map", log);
        // mob relation to hostiles
        parseMobRelations(serverConfig.mob_relations_to_hostiles, this.mobRelationsToHostiles, "mob_relations_to_hostiles", log);
        // mob relation to others
        parseMobRelations(serverConfig.mob_relations_to_other, this.mobRelationsToOther, "mob_relations_to_others", log);
    }

    private static void parseMobRelations(Map<String, TargetHelper.Relation> relationsToParse, Map<EntityType<?>, TargetHelper.Relation> mobRelationMap, String mapName, boolean log) {
        for(Map.Entry<String, TargetHelper.Relation> entry1 : relationsToParse.entrySet()){
            ResourceLocation id1 = ResourceLocation.tryParse(entry1.getKey());
            if(id1 == null){
                if(log) Constants.LOG.error("Could not parse {} entry key {}, not a valid namespaced id", mapName, entry1);
            } else{
                Optional<EntityType<?>> entityTypeValue = BuiltInRegistries.ENTITY_TYPE.getOptional(id1);
                entityTypeValue.ifPresentOrElse(etv -> {
                    mobRelationMap.put(etv, entry1.getValue());
                    if(log) Constants.LOG.debug("Entered {}:{} into the {}!", id1, entry1.getValue(), mapName);
                }, () -> {
                    if(log) Constants.LOG.error("Could not find {} entry key {}, not a valid entity type", mapName, id1);
                });
            }
        }
    }

    public boolean isBlacklistedForBetterCombat(Entity entity){
        if(serverConfig.geckolib_mobs_blacklisted && BMCCompatibilityFlags.isGeckolibLoaded()){
            return GeckolibHelper.isGeoAnimatable(entity);
        } else{
            if(serverConfig.mob_blacklist_as_whitelist){
                return !this.mobBlacklist.contains(entity.getType());
            } else {
                return this.mobBlacklist.contains(entity.getType());
            }
        }
    }

    @Nullable
    public TargetHelper.Relation getMobRelation(EntityType<?> attackerType, EntityType<?> targetType) {
        return this.mobRelations.get(attackerType).get(targetType);
    }

    @Nullable
    public TargetHelper.Relation getMobRelationToPassives(EntityType<?> type) {
        return this.mobRelationsToPassives.get(type);
    }

    @Nullable
    public TargetHelper.Relation getMobRelationToHostiles(EntityType<?> type) {
        return this.mobRelationsToHostiles.get(type);
    }

    @Nullable
    public TargetHelper.Relation getMobRelationToOther(EntityType<?> type) {
        return this.mobRelationsToOther.get(type);
    }
}
