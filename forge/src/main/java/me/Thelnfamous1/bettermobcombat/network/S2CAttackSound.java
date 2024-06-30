package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CAttackSound(int mobId, double x, double y, double z, String soundId, float volume, float pitch, long seed) {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "attack_sound");

    public static S2CAttackSound read(FriendlyByteBuf buffer) {
        int mobId = buffer.readInt();
        double x = buffer.readDouble();
        double y = buffer.readDouble();
        double z = buffer.readDouble();
        String soundId = buffer.readUtf();
        float volume = buffer.readFloat();
        float pitch = buffer.readFloat();
        long seed = buffer.readLong();
        return new S2CAttackSound(mobId, x, y, z, soundId, volume, pitch, seed);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.mobId);
        buffer.writeDouble(this.x);
        buffer.writeDouble(this.y);
        buffer.writeDouble(this.z);
        buffer.writeUtf(this.soundId);
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
        buffer.writeLong(this.seed);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            BetterMobCombatNetworkClient.handlePlaySound(this.mobId(), this.x(), this.y(), this.z(), this.soundId(), this.volume(), this.pitch(), this.seed());
        });
        ctx.get().setPacketHandled(true);
    }

    public double x() {
        return this.x;
    }

    public double y() {
        return this.y;
    }

    public double z() {
        return this.z;
    }

    public String soundId() {
        return this.soundId;
    }

    public float volume() {
        return this.volume;
    }

    public float pitch() {
        return this.pitch;
    }

    public long seed() {
        return this.seed;
    }
}