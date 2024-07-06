package me.Thelnfamous1.bettermobcombat;

import me.Thelnfamous1.bettermobcombat.network.BMCForgeNetwork;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
@Mod(Constants.MOD_ID)
public class BetterMobCombatForge {
    
    public BetterMobCombatForge() {
    
        // This method is invoked by the Forge mod loader when it is ready
        // to load your mod. You can access Forge and Common code in this
        // project.
    
        // Use Forge to bootstrap the Common mod.
        BetterMobCombat.init();
        FMLJavaModLoadingContext.get().getModEventBus().addListener((FMLCommonSetupEvent event) -> BMCForgeNetwork.register());
        MinecraftForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedInEvent event) ->
                Services.PLATFORM.syncServerConfig((ServerPlayer)event.getEntity()));
    }

    @SubscribeEvent
    static void onGatherData(GatherDataEvent event){

    }
}