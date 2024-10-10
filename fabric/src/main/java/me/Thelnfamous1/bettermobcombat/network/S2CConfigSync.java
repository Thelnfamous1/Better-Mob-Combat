package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record S2CConfigSync(String json) implements FabricPacket {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "config_sync");
    public static final PacketType<S2CConfigSync> PACKET_TYPE = PacketType.create(ID, S2CConfigSync::read);

    @Override
    public PacketType<?> getType() {
        return PACKET_TYPE;
    }

    public static S2CConfigSync read(FriendlyByteBuf buffer) {
        String json = buffer.readUtf();
        return new S2CConfigSync(json);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeUtf(this.json);
    }
}
