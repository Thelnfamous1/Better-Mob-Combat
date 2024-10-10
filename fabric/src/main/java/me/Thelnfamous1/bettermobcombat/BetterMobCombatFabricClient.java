package me.Thelnfamous1.bettermobcombat;

import me.Thelnfamous1.bettermobcombat.network.BMCFabricNetwork;
import net.fabricmc.api.ClientModInitializer;

public class BetterMobCombatFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BetterMobCombatClient.init();
        BMCFabricNetwork.registerClientReceivers();
    }
}
