package me.Thelnfamous1.bettermobcombat.platform;

import me.Thelnfamous1.bettermobcombat.BetterMobCombat;
import me.Thelnfamous1.bettermobcombat.BetterMobCombatFabric;
import me.Thelnfamous1.bettermobcombat.network.S2CAttackAnimation;
import me.Thelnfamous1.bettermobcombat.network.S2CAttackSound;
import me.Thelnfamous1.bettermobcombat.network.S2CComboCountSync;
import me.Thelnfamous1.bettermobcombat.network.S2CConfigSync;
import me.Thelnfamous1.bettermobcombat.platform.services.IPlatformHelper;
import net.bettercombat.logic.AnimatedHand;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        for(ServerPlayer tracker : PlayerLookup.tracking(mob)){
            ServerPlayNetworking.send(tracker, new S2CAttackAnimation(mob.getId(), animatedHand, animationName, length, upswing));
        }
    }

    @Override
    public void stopMobAttackAnimation(LivingEntity mob, int downWind) {
        for(ServerPlayer tracker : PlayerLookup.tracking(mob)){
            ServerPlayNetworking.send(tracker, S2CAttackAnimation.stop(mob.getId(), downWind));
        }
    }

    @Override
    public void syncServerConfig(ServerPlayer player) {
        ServerPlayNetworking.send(player, new S2CConfigSync(BetterMobCombat.getServerConfigSerialized()));
    }

    @Override
    public void playMobAttackSound(ServerLevel world, int mobId, double x, double y, double z, String soundId, float volume, float pitch, long seed, float distance, ResourceKey<Level> dimension) {
        for(ServerPlayer around : PlayerLookup.around(world, new Vec3(x, y, z), distance)){
            ServerPlayNetworking.send(around, new S2CAttackSound(mobId, x, y, z, soundId, volume, pitch, seed));
        }
    }

    @Override
    public void syncMobComboCount(LivingEntity mob, int comboCount) {
        for(ServerPlayer tracker : PlayerLookup.tracking(mob)){
            ServerPlayNetworking.send(tracker, new S2CComboCountSync(mob.getId(), comboCount));
        }
    }

    @Override
    public void syncServerConfig() {
        MinecraftServer server = BetterMobCombatFabric.getServer();
        if(server != null){
            for(ServerPlayer player: PlayerLookup.all(server)){
                ServerPlayNetworking.send(player, new S2CConfigSync(BetterMobCombat.getServerConfigSerialized()));
            }
        }
    }

}
