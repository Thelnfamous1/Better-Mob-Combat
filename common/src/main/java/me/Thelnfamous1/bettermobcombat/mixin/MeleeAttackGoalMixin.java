package me.Thelnfamous1.bettermobcombat.mixin;

import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin extends Goal {

    @Shadow @Final protected PathfinderMob mob;

    @Shadow private int ticksUntilNextAttack;

    @Inject(
            method = "checkAndPerformAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity $$0, double $$1, CallbackInfo ci) {
        MobCombatHelper.onHoldingAnimatedAttackWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                ci.cancel();
                if(((MobAttackWindup)m).bettermobcombat$getAttackCooldown() >= 0 && MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange())){
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    this.bettermobcombat$setTicksUntilNextAttack(((MobAttackWindup)m).bettermobcombat$getAttackCooldown());
                }
            }
        });
    }

    @Unique
    protected void bettermobcombat$setTicksUntilNextAttack(int ticksUntilNextAttack){
        this.ticksUntilNextAttack = this.adjustedTickDelay(ticksUntilNextAttack);
    }

    @Inject(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;getAttackReachSqr(Lnet/minecraft/world/entity/LivingEntity;)D"), cancellable = true)
    private void pre_getAttackReachSqr(CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingAnimatedAttackWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange()));
            }
        });
    }

}
