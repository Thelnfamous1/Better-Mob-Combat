package me.Thelnfamous1.bettermobcombat.config;

import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.compatibility.BMCCompatibilityFlags;
import me.Thelnfamous1.bettermobcombat.compatibility.GeckolibHelper;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class BMCServerConfigHelper {
    private final BMCServerConfig serverConfig;
    private final Set<EntityType<?>> mobBlacklist = new HashSet<>();

    public BMCServerConfigHelper(BMCServerConfig serverConfig){
        this.serverConfig = serverConfig;
        for(String entry : serverConfig.mob_blacklist){
            ResourceLocation id = ResourceLocation.tryParse(entry);
            if(id == null){
                Constants.LOG.error("Could not parse mob blacklist entry {}, not a valid namespaced id", entry);
            } else{
                Optional<EntityType<?>> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(id);
                entityType.ifPresentOrElse(et -> {
                    this.mobBlacklist.add(et);
                    Constants.LOG.info("Entered {} into the mob blacklist!", id);
                }, () -> Constants.LOG.error("Could not find mob blacklist entry {}, not a valid entity type", id));
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

}
