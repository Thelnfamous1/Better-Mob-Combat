package me.Thelnfamous1.bettermobcombat;

import me.Thelnfamous1.bettermobcombat.client.BetterMobCombatEvents;
import me.Thelnfamous1.bettermobcombat.compatibility.BMCCompatibilityFlags;
import me.Thelnfamous1.bettermobcombat.config.BMCServerConfig;
import me.Thelnfamous1.bettermobcombat.config.BMCServerConfigHelper;
import me.Thelnfamous1.bettermobcombat.config.BMCServerConfigWrapper;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.client.BetterCombatClientEvents;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.logic.PlayerAttackHelper;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;

import java.util.function.Function;

// This class is part of the common project meaning it is shared between all supported loaders. Code written here can only
// import and access the vanilla codebase, libraries used by vanilla, and optionally third party libraries that provide
// common compatible binaries. This means common code can not directly use loader specific concepts such as Forge events
// however it will be compatible with all supported mod loaders.
public class BetterMobCombat {

    private static BMCServerConfig serverConfig;
    private static BMCServerConfigHelper serverConfigHelper;

    private static String serverConfigSerialized = "";

    // The loader specific projects are able to import and use any code from the common project. This allows you to
    // write the majority of your code here and load it from your loader specific projects. This example has some
    // code that gets invoked by the entry point of the loader specific projects.
    public static void init() {
        // It is common for all supported loaders to provide a similar feature that can not be used directly in the
        // common code. A popular way to get around this is using Java's built-in service loader feature to create
        // your own abstraction layer. You can learn more about this in our provided services class. In this example
        // we have an interface in the common code and use a loader specific implementation to delegate our call to
        // the platform specific approach.
        ConfigHolder<BMCServerConfigWrapper> configHolder = AutoConfig.register(BMCServerConfigWrapper.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new));
        configHolder.registerSaveListener(((ch, scw) -> onConfigUpdated(scw)));
        // This does not run when the config is first loaded, have to manually update config below
        configHolder.registerLoadListener(((ch, scw) -> onConfigUpdated(scw)));
        // Manual server config update
        updateServerConfig(configHolder.getConfig().server, false);

        BetterMobCombatEvents.ATTACK_START.register((mob, attackHand) -> debugTriggeredAttack(mob, attackHand, MobAttackHelper::getAttackCooldownTicksCapped));
        BetterCombatClientEvents.ATTACK_START.register((player, attackHand) -> debugTriggeredAttack(player, attackHand, PlayerAttackHelper::getAttackCooldownTicksCapped));

        BMCCompatibilityFlags.initialize();
    }

    private static InteractionResult onConfigUpdated(BMCServerConfigWrapper scw) {
        updateServerConfig(scw.server, true);
        Services.PLATFORM.syncServerConfig(); // Nothing should happen if the server has not started
        return InteractionResult.PASS;
    }

    private static <T extends LivingEntity> void debugTriggeredAttack(T entity, AttackHand attackHand, Function<T, Float> attackCooldownGetter) {
        float upswingRate = (float) attackHand.upswingRate();
        float attackCooldownTicksFloat = attackCooldownGetter.apply(entity);
        String animationName = attackHand.attack().animation();
        boolean isOffHand = attackHand.isOffHand();
        AnimatedHand animatedHand = AnimatedHand.from(isOffHand, attackHand.attributes().isTwoHanded());
        Constants.LOG.debug("Triggering attack animation for {} with AnimatedHand {}, animation name {}, length {}, upswing {}", entity, animatedHand, animationName, attackCooldownTicksFloat, upswingRate);
    }

    public static BMCServerConfig getServerConfig() {
        return serverConfig;
    }

    public static void updateServerConfig(BMCServerConfig config, boolean log) {
        serverConfig = config;
        serverConfigHelper = new BMCServerConfigHelper(config);
        serverConfigSerialized = config.serialize();
        if(log) Constants.LOG.info("Server config updated! {}", serverConfigSerialized);
    }

    public static String getServerConfigSerialized() {
        return serverConfigSerialized;
    }


    public static BMCServerConfigHelper getServerConfigHelper() {
        return serverConfigHelper;
    }
}