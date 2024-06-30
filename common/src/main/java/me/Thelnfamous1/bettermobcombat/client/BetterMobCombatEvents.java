package me.Thelnfamous1.bettermobcombat.client;

import net.bettercombat.api.AttackHand;
import net.bettercombat.api.event.Publisher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BetterMobCombatEvents {
    public static final Publisher<MobAttackStart> ATTACK_START = new Publisher<>();
    public static final Publisher<MobAttackHit> ATTACK_HIT = new Publisher<>();

    public BetterMobCombatEvents() {
    }

    @FunctionalInterface
    public interface MobAttackHit {
        void onMobAttackHit(Mob mob, AttackHand attackHand, List<Entity> targets, @Nullable Entity target);
    }

    @FunctionalInterface
    public interface MobAttackStart {
        void onMobAttackStart(Mob mob, AttackHand attackHand);
    }
}