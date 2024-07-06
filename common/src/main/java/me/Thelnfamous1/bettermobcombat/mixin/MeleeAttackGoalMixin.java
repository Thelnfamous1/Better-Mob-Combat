package me.Thelnfamous1.bettermobcombat.mixin;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
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

    @WrapWithCondition(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;checkAndPerformAttack(Lnet/minecraft/world/entity/LivingEntity;D)V"))
    private boolean pre_checkAndPerformAttack(MeleeAttackGoal goal, LivingEntity attacker, double distance){
        return !this.bettermobcombat$useBetterCombatAttackCheck();
    }

    @Unique
    protected boolean bettermobcombat$useBetterCombatAttackCheck() {
        return MobCombatHelper.canUseBetterCombatWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                if (((MobAttackWindup) m).bettermobcombat$getAttackCooldown() >= 0 && MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange())) {
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    this.bettermobcombat$setTicksUntilNextAttack(((MobAttackWindup) m).bettermobcombat$getAttackCooldown());
                    this.bettermobcombat$postBetterCombatAttack();
                }
                return true; // return true as long as there is a current BC attack that the mob can perform
            }
            return false;
        });
    }

    @Unique
    protected void bettermobcombat$postBetterCombatAttack() {
    }

    // backup, should almost always never be called
    @Inject(
            method = "checkAndPerformAttack",
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity $$0, double $$1, CallbackInfo ci) {
        if(this.bettermobcombat$useBetterCombatAttackCheck()){
            ci.cancel();
        }
    }

    @Unique
    protected void bettermobcombat$setTicksUntilNextAttack(int ticksUntilNextAttack){
        this.ticksUntilNextAttack = this.adjustedTickDelay(ticksUntilNextAttack);
    }

    @Inject(method = "canUse", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/ai/goal/MeleeAttackGoal;getAttackReachSqr(Lnet/minecraft/world/entity/LivingEntity;)D"), cancellable = true)
    private void pre_getAttackReachSqr_canUse(CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange()));
            }
        });
    }

}
