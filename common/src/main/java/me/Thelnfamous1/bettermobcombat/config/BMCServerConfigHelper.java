package me.Thelnfamous1.bettermobcombat.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
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
    public static final Codec<TargetHelper.Relation> RELATION_CODEC = Codec.STRING.comapFlatMap(BMCServerConfigHelper::readRelation, Enum::name).stable();
    public static final Codec<Map<EntityType<?>, TargetHelper.Relation>> MOB_RELATIONS_CODEC = Codec.unboundedMap(BuiltInRegistries.ENTITY_TYPE.byNameCodec(), RELATION_CODEC);
    public static final Codec<Map<String, TargetHelper.Relation>> MOB_RELATIONS_STRING_CODEC = Codec.unboundedMap(Codec.STRING, RELATION_CODEC);
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
        serverConfig.mob_relations.forEach((key, value) -> {
            Codec<? extends EntityType<?>> resourceLocationToEntityType = getResourceLocationToEntityTypeCodec();
            EntityType<?> entityType = resourceLocationToEntityType.parse(JsonOps.INSTANCE, new JsonPrimitive(key)).result().orElse(null);
            if(entityType == null) {
                if(log) Constants.LOG.error("Could not parse {} entry key {}, not a valid namespaced id", "mob_relations", key);
                return;
            }
            Map<EntityType<?>, TargetHelper.Relation> modelModifier = MOB_RELATIONS_CODEC.parse(JsonOps.INSTANCE, new Gson().fromJson(value, JsonObject.class)).result().orElse(null);
            if(modelModifier == null){
                if(log) Constants.LOG.error("Could not parse {} entry value {} mapped to {}, not a valid map of entity types to target relations", "mob_relations", key, value);
                return;
            }
            Constants.LOG.debug("Entered {}:{} into the " + "mob_relations" + " map!", key, value);
            this.mobRelations.put(entityType, modelModifier);
        });
        // mob relation to passives
        parseMobRelations(serverConfig.mob_relations_to_passives, this.mobRelationsToPassives, "mob_relations_to_passives map", log);
        // mob relation to hostiles
        parseMobRelations(serverConfig.mob_relations_to_hostiles, this.mobRelationsToHostiles, "mob_relations_to_hostiles", log);
        // mob relation to others
        parseMobRelations(serverConfig.mob_relations_to_other, this.mobRelationsToOther, "mob_relations_to_others", log);
    }

    private static Codec<? extends EntityType<?>> getResourceLocationToEntityTypeCodec() {
        return ResourceLocation.CODEC.comapFlatMap(rl -> {
            if (BuiltInRegistries.ENTITY_TYPE.containsKey(rl)) {
                return DataResult.success(BuiltInRegistries.ENTITY_TYPE.get(rl));
            }
            return DataResult.error(() -> String.format("Could not parse %s, not a valid entity type", rl));
        }, BuiltInRegistries.ENTITY_TYPE::getKey);
    }

    private static DataResult<TargetHelper.Relation> readRelation(String relation) {
        try {
            return DataResult.success(TargetHelper.Relation.valueOf(relation.toUpperCase(Locale.ROOT)));
        } catch (Exception e) {
            return DataResult.error(() -> "Not a valid target relation: " + relation);
        }
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
