package me.Thelnfamous1.bettermobcombat.mixin;

import me.Thelnfamous1.bettermobcombat.logic.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MeleeAttackGoal.class)
public abstract class MeleeAttackGoalMixin {

    @Shadow @Final protected PathfinderMob mob;

    @Shadow protected abstract void resetAttackCooldown();

    @Inject(
            method = "checkAndPerformAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity $$0, double $$1, CallbackInfo ci) {
        MobCombatHelper.onHoldingAnimatedAttackWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)this.mob).getCurrentAttack();
            if(currentAttack != null){
                ci.cancel();
                if(((MobAttackWindup)m).bettermobcombat$getAttackCooldown() >= 0 && MobCombatHelper.isWithinAttackRange(m, this.mob.getTarget(), currentAttack.attack(), wa.attackRange())){
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    this.resetAttackCooldown();
                }
            }
        });
    }

    @Inject(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;getAttackReachSqr(Lnet/minecraft/world/entity/LivingEntity;)D"), cancellable = true)
    private void pre_getAttackReachSqr(CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingAnimatedAttackWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)this.mob).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, this.mob.getTarget(), currentAttack.attack(), wa.attackRange()));
            }
        });
    }

}
