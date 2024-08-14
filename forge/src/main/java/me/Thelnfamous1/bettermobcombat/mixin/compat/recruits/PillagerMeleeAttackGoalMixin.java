package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.talhanation.recruits.entities.ai.pillager.PillagerMeleeAttackGoal;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Pillager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = PillagerMeleeAttackGoal.class, remap = false)
public abstract class PillagerMeleeAttackGoalMixin extends Goal {
    @Shadow
    @Final
    protected Pillager mob;

    @Shadow private int ticksUntilNextAttack;

    @Inject(method = "canUse", remap = true, at = @At(value = "INVOKE",
            target = "Lcom/talhanation/recruits/entities/ai/pillager/PillagerMeleeAttackGoal;getAttackReachSqr(Lnet/minecraft/world/entity/LivingEntity;)D", remap = false), cancellable = true)
    private void pre_getAttackReachSqr_canUse(CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange()));
            }
        });
    }

    @WrapWithCondition(method = "tick", remap = true, at = @At(value = "INVOKE",
            target = "Lcom/talhanation/recruits/entities/ai/pillager/PillagerMeleeAttackGoal;checkAndPerformAttack(Lnet/minecraft/world/entity/LivingEntity;D)V", remap = false))
    private boolean pre_checkAndPerformAttack(PillagerMeleeAttackGoal instance, LivingEntity target, double distance){
        return !this.bettermobcombat$useBetterCombatAttackCheck(target);
    }

    @Unique
    protected boolean bettermobcombat$useBetterCombatAttackCheck(LivingEntity target) {
        return MobCombatHelper.canUseBetterCombatWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                if (MobCombatHelper.isAttackReady(m) && MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange())) {
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
            remap = false,
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity target, double $$1, CallbackInfo ci) {
        if(this.bettermobcombat$useBetterCombatAttackCheck(target)){
            ci.cancel();
        }
    }

    @Unique
    protected void bettermobcombat$setTicksUntilNextAttack(int ticksUntilNextAttack){
        this.ticksUntilNextAttack = this.adjustedTickDelay(ticksUntilNextAttack);
    }
}
