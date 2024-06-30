package me.Thelnfamous1.bettermobcombat.logic;

import me.Thelnfamous1.bettermobcombat.api.MobAttackRangeExtensions;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.api.client.AttackRangeExtensions;
import net.bettercombat.client.collision.CollisionHelper;
import net.bettercombat.client.collision.OrientedBoundingBox;
import net.bettercombat.client.collision.TargetFinder;
import net.bettercombat.client.collision.WeaponHitBoxes;
import net.bettercombat.logic.TargetHelper;
import net.bettercombat.logic.TargetHelper.Relation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

public class MobTargetFinder {
    public MobTargetFinder() {
    }

    public static TargetFinder.TargetResult findAttackTargetResult(LivingEntity mob, @Nullable Entity cursorTarget, WeaponAttributes.Attack attack, double attackRange) {
        Vec3 origin = getInitialTracingPoint(mob);
        List<Entity> entities = getInitialTargets(mob, cursorTarget, attackRange);
        if (!AttackRangeExtensions.sources().isEmpty()) {
            attackRange = applyAttackRangeModifiers(mob, attackRange);
        }

        boolean isSpinAttack = attack.angle() > 180.0;
        Vec3 size = WeaponHitBoxes.createHitbox(attack.hitbox(), attackRange, isSpinAttack);
        OrientedBoundingBox obb = new OrientedBoundingBox(origin, size, mob.getXRot(), mob.getYRot());
        if (!isSpinAttack) {
            obb = obb.offsetAlongAxisZ(size.z / 2.0);
        }

        obb.updateVertex();
        CollisionFilter collisionFilter = new CollisionFilter(obb);
        entities = collisionFilter.filter(entities, mob);
        RadialFilter radialFilter = new RadialFilter(origin, obb.axisZ, attackRange, attack.angle());
        entities = radialFilter.filter(entities, mob);
        return new TargetFinder.TargetResult(entities, obb);
    }

    public static double applyAttackRangeModifiers(LivingEntity mob, double attackRange) {
        MobAttackRangeExtensions.Context context = new MobAttackRangeExtensions.Context(mob, attackRange);
        List<AttackRangeExtensions.Modifier> modifiers = MobAttackRangeExtensions.sources().stream().map((function) -> function.apply(context)).sorted(Comparator.comparingInt(AttackRangeExtensions.Modifier::operationOrder)).toList();
        double result = attackRange;

        for (AttackRangeExtensions.Modifier modifier : modifiers) {
            switch (modifier.operation()) {
                case ADD -> result += modifier.value();
                case MULTIPLY -> result *= modifier.value();
            }
        }

        return result;
    }

    public static List<Entity> findAttackTargets(LivingEntity mob, @Nullable Entity cursorTarget, WeaponAttributes.Attack attack, double attackRange) {
        return findAttackTargetResult(mob, cursorTarget, attack, attackRange).entities;
    }

    public static Vec3 getInitialTracingPoint(LivingEntity mob) {
        double shoulderHeight = (double)mob.getBbHeight() * 0.15 * (double)mob.getScale();
        return mob.getEyePosition().subtract(0.0, shoulderHeight, 0.0);
    }

    public static List<Entity> getInitialTargets(LivingEntity mob, @Nullable Entity cursorTarget, double attackRange) {
        AABB box = mob.getBoundingBox().inflate(attackRange * (double)BetterCombat.config.target_search_range_multiplier + 1.0);
        List<Entity> entities = mob.level().getEntities(mob, box, (entity) -> !entity.isSpectator() && entity.isPickable()).stream().filter((entity) -> entity != mob && entity != cursorTarget && entity.isAttackable() && (!entity.equals(mob.getVehicle()) || TargetHelper.isAttackableMount(entity)) && MobTargetHelper.getRelation(mob, entity) == Relation.HOSTILE).collect(Collectors.toList());
        if (cursorTarget != null && cursorTarget.isAttackable()) {
            entities.add(cursorTarget);
        }

        return entities;
    }

    public static class CollisionFilter implements Filter {
        private final OrientedBoundingBox obb;

        public CollisionFilter(OrientedBoundingBox obb) {
            this.obb = obb;
        }

        @Override
        public boolean test(Entity target, LivingEntity mob) {
            return this.obb.intersects(target.getBoundingBox().inflate(target.getPickRadius()))
                    || this.obb.contains(target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0));
        }
    }

    public static class RadialFilter implements Filter {
        private final Vec3 origin;
        private final Vec3 orientation;
        private final double attackRange;
        private final double attackAngle;

        public RadialFilter(Vec3 origin, Vec3 orientation, double attackRange, double attackAngle) {
            this.origin = origin;
            this.orientation = orientation;
            this.attackRange = attackRange;
            this.attackAngle = Mth.clamp(attackAngle, 0.0, 360.0);
        }

        @Override
        public boolean test(Entity target, LivingEntity mob) {
            double maxAngleDif = this.attackAngle / 2.0;
            Vec3 distanceVector = CollisionHelper.distanceVector(this.origin, target.getBoundingBox());
            Vec3 positionVector = target.position().add(0.0, target.getBbHeight() / 2.0F, 0.0).subtract(this.origin);
            return distanceVector.length() <= this.attackRange
                    && (this.attackAngle == 0.0 || CollisionHelper.angleBetween(positionVector, this.orientation) <= maxAngleDif || CollisionHelper.angleBetween(distanceVector, this.orientation) <= maxAngleDif)
                    && (BetterCombat.config.allow_attacking_thru_walls || rayContainsNoObstacle(this.origin, this.origin.add(distanceVector), mob) || rayContainsNoObstacle(this.origin, this.origin.add(positionVector), mob));
        }

        private static boolean rayContainsNoObstacle(Vec3 start, Vec3 end, LivingEntity mob) {
            BlockHitResult hit = mob.level().clip(new ClipContext(start, end, Block.COLLIDER, Fluid.NONE, mob));
            return hit.getType() != Type.BLOCK;
        }
    }

    public interface Filter extends BiPredicate<Entity, LivingEntity> {
        default List<Entity> filter(List<Entity> targets, LivingEntity mob) {
            return targets.stream().filter((target) -> this.test(target, mob)).collect(Collectors.toList());
        }
    }
}