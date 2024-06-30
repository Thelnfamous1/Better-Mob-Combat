package me.Thelnfamous1.bettermobcombat.platform;

import me.Thelnfamous1.bettermobcombat.BetterMobCombatCommon;
import me.Thelnfamous1.bettermobcombat.network.BetterMobCombatForgeNetwork;
import me.Thelnfamous1.bettermobcombat.network.S2CAttackAnimation;
import me.Thelnfamous1.bettermobcombat.network.S2CAttackSound;
import me.Thelnfamous1.bettermobcombat.network.S2CConfigSync;
import me.Thelnfamous1.bettermobcombat.platform.services.IPlatformHelper;
import net.bettercombat.logic.AnimatedHand;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
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

    @Override
    public void updateServerConfig(ServerPlayer player) {
        BetterMobCombatForgeNetwork.SYNC_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new S2CConfigSync(BetterMobCombatCommon.getServerConfigSerialized()));
    }

    @Override
    public void playMobAttackSound(ServerLevel world, int mobId, double x, double y, double z, String soundId, float volume, float pitch, long seed, float distance, ResourceKey<Level> dimension) {
        BetterMobCombatForgeNetwork.SYNC_CHANNEL.send(PacketDistributor.NEAR.with(() -> PacketDistributor.TargetPoint.p(x, y, z, distance, dimension).get()),
                new S2CAttackSound(mobId, x, y, z, soundId, volume, pitch, seed));
    }

}