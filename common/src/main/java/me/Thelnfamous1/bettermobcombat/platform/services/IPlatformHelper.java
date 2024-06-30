package me.Thelnfamous1.bettermobcombat.platform.services;

import net.bettercombat.logic.AnimatedHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {

        return isDevelopmentEnvironment() ? "development" : "production";
    }

    boolean isCastingSpell(LivingEntity mob);

    void playMobAttackAnimation(LivingEntity mob, AnimatedHand animatedHand, String animationName, float length, float upswing);

    void stopMobAttackAnimation(LivingEntity mob, int downWind);

    void updateServerConfig(ServerPlayer player);

    void playMobAttackSound(ServerLevel world, int mobId, double x, double y, double z, String soundId, float volume, float pitch, long seed, float distance, ResourceKey<Level> dimension);
}