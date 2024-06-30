package me.Thelnfamous1.bettermobcombat.mixin;

import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.EntityTracker;
import net.minecraft.world.entity.ai.behavior.MeleeAttack;
import net.minecraft.world.entity.ai.behavior.declarative.BehaviorBuilder;
import net.minecraft.world.entity.ai.behavior.declarative.MemoryAccessor;
import net.minecraft.world.entity.ai.memory.NearestVisibleLivingEntities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttack.class)
public abstract class MeleeAttackMixin {

    @Shadow
    private static boolean isHoldingUsableProjectileWeapon(Mob $$0) {
        return false;
    }

    @Inject(method = "lambda$create$0", at = @At(value = "HEAD"), cancellable = true)
    private static void pre_executeMeleeAttack(BehaviorBuilder.Instance instance, MemoryAccessor attackTarget, MemoryAccessor nvle, MemoryAccessor lookTarget, MemoryAccessor attackCoolingDown, int $$5x, ServerLevel $$6, Mob mob, long $$8, CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingAnimatedAttackWeapon(mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                LivingEntity target = (LivingEntity)instance.get(attackTarget);
                if(!isHoldingUsableProjectileWeapon(m)
                        && ((MobAttackWindup)m).bettermobcombat$getAttackCooldown() >= 0
                        && m.isWithinMeleeAttackRange(target)
                        && ((NearestVisibleLivingEntities)instance.get(nvle)).contains(target)){
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    lookTarget.set(new EntityTracker(target, true));
                    attackCoolingDown.setWithExpiry(true, ((MobAttackWindup)m).bettermobcombat$getAttackCooldown());
                    cir.setReturnValue(true);
                } else{
                    cir.setReturnValue(false);
                }
            }
        });
    }
}
