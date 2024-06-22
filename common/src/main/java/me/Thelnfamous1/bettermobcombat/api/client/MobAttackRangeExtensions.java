package me.Thelnfamous1.bettermobcombat.api.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import net.bettercombat.api.client.AttackRangeExtensions;
import net.minecraft.world.entity.LivingEntity;

public class MobAttackRangeExtensions {
    private static final ArrayList<Function<Context, AttackRangeExtensions.Modifier>> sources = new ArrayList<>();

    public MobAttackRangeExtensions() {
    }

    public static void register(Function<Context, AttackRangeExtensions.Modifier> source) {
        sources.add(source);
    }

    public static List<Function<Context, AttackRangeExtensions.Modifier>> sources() {
        return sources;
    }

    public record Context(LivingEntity mob, double attackRange) {
        public Context(LivingEntity mob, double attackRange) {
            this.mob = mob;
            this.attackRange = attackRange;
        }

        public LivingEntity mob() {
            return this.mob;
        }

        public double attackRange() {
            return this.attackRange;
        }
    }
}