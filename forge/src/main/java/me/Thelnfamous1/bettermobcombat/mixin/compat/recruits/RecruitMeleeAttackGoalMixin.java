package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.RecruitMeleeAttackGoal;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = RecruitMeleeAttackGoal.class, remap = false)
public abstract class RecruitMeleeAttackGoalMixin {

    @Shadow @Final protected AbstractRecruitEntity recruit;

    @WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lcom/talhanation/recruits/util/AttackUtil;getAttackReachSqr(Lnet/minecraft/world/entity/LivingEntity;)D"))
    private double wrap_getAttackReachSqr_canUse(LivingEntity attacker, Operation<Double> original){
        return this.bettermobcombat$wrapGetAttackReachSqr(attacker, original);
    }

    @WrapOperation(method = "tick", at = @At(value = "INVOKE", target = "Lcom/talhanation/recruits/util/AttackUtil;getAttackReachSqr(Lnet/minecraft/world/entity/LivingEntity;)D"))
    private double wrap_getAttackReachSqr_tick(LivingEntity attacker, Operation<Double> original){
        return this.bettermobcombat$wrapGetAttackReachSqr(attacker, original);
    }

    @Unique
    private double bettermobcombat$wrapGetAttackReachSqr(LivingEntity attacker, Operation<Double> original) {
        return MobCombatHelper.applyWithBetterCombatWeapon(
                this.recruit,
                (m, wa) -> {
                    AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
                    if (currentAttack != null) {
                        if (MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange())) {
                            return Double.MAX_VALUE; // force distance check success
                        }
                        return -1D; // force distance check failure
                    }
                    return original.call(m);
                },
                () -> original.call(attacker));
    }
}
