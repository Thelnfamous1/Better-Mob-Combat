package me.Thelnfamous1.bettermobcombat.platform;

import me.Thelnfamous1.bettermobcombat.platform.services.IPlatformHelper;
import net.bettercombat.logic.AnimatedHand;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isCastingSpell(LivingEntity mob) {
        return false;
    }

    @Override
    public void playMobAttackAnimation(LivingEntity mob, AnimatedHand animatedHand, String animationName, float length, float upswing) {

    }

    @Override
    public void stopMobAttackAnimation(LivingEntity mob, int downWind) {

    }

    @Override
    public void updateServerConfig(ServerPlayer player) {

    }
}
