package me.Thelnfamous1.bettermobcombat.platform;

import me.Thelnfamous1.bettermobcombat.network.BetterMobCombatForgeNetwork;
import me.Thelnfamous1.bettermobcombat.network.S2CAttackAnimation;
import me.Thelnfamous1.bettermobcombat.platform.services.IPlatformHelper;
import net.bettercombat.logic.AnimatedHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.network.PacketDistributor;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {

        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {

        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {

        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isCastingSpell(LivingEntity mob) {
        return false;
    }

    @Override
    public void playMobAttackAnimation(LivingEntity mob, AnimatedHand animatedHand, String animationName, float length, float upswing) {
        S2CAttackAnimation packet = new S2CAttackAnimation(mob.getId(), animatedHand, animationName, length, upswing);
        BetterMobCombatForgeNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> mob), packet);
    }

    @Override
    public void stopMobAttackAnimation(LivingEntity mob, int downWind) {
        S2CAttackAnimation packet = S2CAttackAnimation.stop(mob.getId(), downWind);
        BetterMobCombatForgeNetwork.SYNC_CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> mob), packet);
    }
}