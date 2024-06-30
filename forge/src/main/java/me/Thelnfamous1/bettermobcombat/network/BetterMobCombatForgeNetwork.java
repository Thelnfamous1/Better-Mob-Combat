package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.Optional;

public class BetterMobCombatForgeNetwork {

    public static final ResourceLocation CHANNEL_NAME = new ResourceLocation(Constants.MOD_ID, "sync_channel");
    private static final String PROTOCOL_VERSION = "1.0";
    public static final SimpleChannel SYNC_CHANNEL = NetworkRegistry.newSimpleChannel(
            CHANNEL_NAME, () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );
    private static int INDEX = 0;

    public static void register() {
        SYNC_CHANNEL.registerMessage(INDEX++, S2CAttackAnimation.class, S2CAttackAnimation::write, S2CAttackAnimation::read, S2CAttackAnimation::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        SYNC_CHANNEL.registerMessage(INDEX++, S2CConfigSync.class, S2CConfigSync::write, S2CConfigSync::read, S2CConfigSync::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        SYNC_CHANNEL.registerMessage(INDEX++, S2CAttackSound.class, S2CAttackSound::write, S2CAttackSound::read, S2CAttackSound::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        SYNC_CHANNEL.registerMessage(INDEX++, S2CComboCountSync.class, S2CComboCountSync::write, S2CComboCountSync::read, S2CComboCountSync::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
    }
}
