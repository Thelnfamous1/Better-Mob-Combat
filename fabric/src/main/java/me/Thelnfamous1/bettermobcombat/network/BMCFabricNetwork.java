package me.Thelnfamous1.bettermobcombat.network;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

public class BMCFabricNetwork {

    public static void registerClientReceivers(){
        ClientPlayNetworking.registerGlobalReceiver(S2CAttackAnimation.PACKET_TYPE, (packet, player, responseSender) -> {
            BMCClientNetworkHandler.handleAttackAnimation(packet.mobId(), packet.animationName(), packet.length(), packet.animatedHand(), packet.upswing());
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CAttackSound.PACKET_TYPE, (packet, player, responseSender) -> {
            BMCClientNetworkHandler.handlePlaySound(packet.mobId(), packet.x(), packet.y(), packet.z(), packet.soundId(), packet.volume(), packet.pitch(), packet.seed());
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CComboCountSync.PACKET_TYPE, (packet, player, responseSender) -> {
            BMCClientNetworkHandler.handleComboSync(packet.mobId(), packet.comboCount());
        });
        ClientPlayNetworking.registerGlobalReceiver(S2CConfigSync.PACKET_TYPE, (packet, player, responseSender) -> {
            BMCClientNetworkHandler.handleConfigSync(packet.json());
        });
    }
}
