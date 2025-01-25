package me.Thelnfamous1.bettermobcombat.mixin.compat.modulargolems;

import dev.xkmc.modulargolems.content.entity.common.AbstractGolemEntity;
import dev.xkmc.modulargolems.content.entity.goals.GolemMeleeGoal;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import me.Thelnfamous1.bettermobcombat.mixin.MeleeAttackGoalMixin;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(GolemMeleeGoal.class)
public abstract class GolemMeleeGoalMixin extends MeleeAttackGoalMixin {

    @Shadow(remap = false) @Final private AbstractGolemEntity<?, ?> golem;

    @Inject(method = "adjustedTickDelay", at = @At("HEAD"), cancellable = true)
    private void pre_adjustedTickDelay(int tick, CallbackInfoReturnable<Integer> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.golem, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(super.adjustedTickDelay(tick));
            }
        });
    }

    @Override
    protected boolean bettermobcombat$allowCheckAndPerformAttackCall(LivingEntity target) {
        return true;
    }

    @Inject(method = "canReachTarget", at = @At("HEAD"), cancellable = true, remap = false)
    private void pre_canReachTarget(LivingEntity le, CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.golem, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, le, currentAttack.attack(), wa.attackRange()));
            }
        });
    }
}
