package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.network.Packets;
import net.fabricmc.fabric.api.networking.v1.FabricPacket;
import net.fabricmc.fabric.api.networking.v1.PacketType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record S2CAttackAnimation(int mobId, AnimatedHand animatedHand, String animationName, float length, float upswing) implements FabricPacket {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "attack_animation");
    public static final PacketType<S2CAttackAnimation> PACKET_TYPE = PacketType.create(ID, S2CAttackAnimation::read);

    @Override
    public PacketType<?> getType() {
        return PACKET_TYPE;
    }

    public static S2CAttackAnimation read(FriendlyByteBuf buffer) {
        int mobId = buffer.readInt();
        AnimatedHand animatedHand = AnimatedHand.values()[buffer.readInt()];
        String animationName = buffer.readUtf();
        float length = buffer.readFloat();
        float upswing = buffer.readFloat();
        return new S2CAttackAnimation(mobId, animatedHand, animationName, length, upswing);
    }

    public static S2CAttackAnimation stop(int mobId, int length) {
        return new S2CAttackAnimation(mobId, AnimatedHand.MAIN_HAND, Packets.AttackAnimation.StopSymbol, (float)length, 0.0F);
    }

    public void write(FriendlyByteBuf buffer) {
        buffer.writeInt(this.mobId);
        buffer.writeInt(this.animatedHand.ordinal());
        buffer.writeUtf(this.animationName);
        buffer.writeFloat(this.length);
        buffer.writeFloat(this.upswing);
    }

    public int mobId() {
            return this.mobId;
    }

    public AnimatedHand animatedHand() {
            return this.animatedHand;
    }
    public String animationName() {
        return this.animationName;
    }

    public float length() {
        return this.length;
    }

    public float upswing() {
        return this.upswing;
    }
}