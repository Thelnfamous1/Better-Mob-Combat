package me.Thelnfamous1.bettermobcombat.network;

import me.Thelnfamous1.bettermobcombat.Constants;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.network.Packets;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public record S2CAttackAnimation(int mobId, AnimatedHand animatedHand, String animationName, float length, float upswing) {
    public static ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "attack_animation");

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

    public void handle(Supplier<NetworkEvent.Context> ctx){
        ctx.get().enqueueWork(() -> {
            BMCClientNetworkHandler.handleAttackAnimation(this.mobId(), this.animationName(), this.length(), this.animatedHand(), this.upswing());
        });
        ctx.get().setPacketHandled(true);
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