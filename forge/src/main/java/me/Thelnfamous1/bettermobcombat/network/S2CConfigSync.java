package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CConfigSync(String json) {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "config_sync");

    public static S2CConfigSync read(FriendlyByteBuf buffer) {
        String json = buffer.readUtf();
        return new S2CConfigSync(json);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.json);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            BMCClientNetworkHandler.handleConfigSync(this.json);
        });
        ctx.get().setPacketHandled(true);
    }
}
