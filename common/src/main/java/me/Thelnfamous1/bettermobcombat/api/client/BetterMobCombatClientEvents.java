package me.Thelnfamous1.bettermobcombat.api.client;

import net.bettercombat.api.AttackHand;
import net.bettercombat.api.event.Publisher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BetterMobCombatClientEvents {
    public static final Publisher<MobAttackStart> ATTACK_START = new Publisher<>();
    public static final Publisher<MobAttackHit> ATTACK_HIT = new Publisher<>();

    public BetterMobCombatClientEvents() {
    }

    @FunctionalInterface
    public interface MobAttackHit {
        void onPlayerAttackStart(LivingEntity var1, AttackHand var2, List<Entity> var3, @Nullable Entity var4);
    }

    @FunctionalInterface
    public interface MobAttackStart {
        void onPlayerAttackStart(LivingEntity var1, AttackHand var2);
    }
}