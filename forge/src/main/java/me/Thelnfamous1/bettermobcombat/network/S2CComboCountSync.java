package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CComboCountSync(int mobId, int comboCount) {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "combo_count_sync");

    public static S2CComboCountSync read(FriendlyByteBuf buffer) {
        int mobId = buffer.readInt();
        int comboCount = buffer.readInt();
        return new S2CComboCountSync(mobId, comboCount);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.mobId);
        buffer.writeInt(this.comboCount);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            BetterMobCombatNetworkClient.handleComboSync(this.mobId(), this.comboCount());
        });
        ctx.get().setPacketHandled(true);
    }

    public int mobId() {
            return this.mobId;
    }

    public int comboCount() {
        return this.comboCount;
    }
}