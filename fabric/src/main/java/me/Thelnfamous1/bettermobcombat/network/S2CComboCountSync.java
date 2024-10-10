package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record S2CComboCountSync(int mobId, int comboCount) implements FabricPacket {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "combo_count_sync");
    public static final PacketType<S2CComboCountSync> PACKET_TYPE = PacketType.create(ID, S2CComboCountSync::read);

    @Override
    public PacketType<?> getType() {
        return PACKET_TYPE;
    }

    public static S2CComboCountSync read(FriendlyByteBuf buffer) {
        int mobId = buffer.readInt();
        int comboCount = buffer.readInt();
        return new S2CComboCountSync(mobId, comboCount);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.mobId);
        buffer.writeInt(this.comboCount);
    }

    public int mobId() {
            return this.mobId;
    }

    public int comboCount() {
        return this.comboCount;
    }
}