package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.util.AttackUtil;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = AttackUtil.class, remap = false)
public abstract class AttackUtilMixin {

    @Inject(method = "checkAndPerformAttack", remap = false, at = @At("HEAD"), cancellable = true)
    private static void pre_checkAndPerformAttack(double distanceSqrToTarget, double reach, AbstractRecruitEntity recruit, LivingEntity target, CallbackInfo ci){
        MobCombatHelper.onHoldingBetterCombatWeapon(recruit, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                bettermobcombat$performBetterCombatAttack(recruit, wa, currentAttack);
                ci.cancel();
            }
        });
    }

    @Unique
    private static void bettermobcombat$performBetterCombatAttack(AbstractRecruitEntity recruit, WeaponAttributes wa, AttackHand currentAttack) {
        if (MobCombatHelper.isAttackReady(recruit) && MobCombatHelper.isWithinAttackRange(recruit, recruit.getTarget(), currentAttack.attack(), wa.attackRange())) {
            ((MobAttackWindup) recruit).bettermobcombat$startUpswing(wa);
            recruit.attackCooldown = ((MobAttackWindup) recruit).bettermobcombat$getAttackCooldown();
        }
    }

    @Inject(method = "performAttack", remap = false, at = @At("HEAD"), cancellable = true)
    private static void pre_performAttack(AbstractRecruitEntity recruit, LivingEntity target, CallbackInfo ci){
        if(bettermobcombat$useBetterCombatAttackCheck(recruit)) ci.cancel();
    }

    @Unique
    private static boolean bettermobcombat$useBetterCombatAttackCheck(AbstractRecruitEntity recruit) {
        return MobCombatHelper.canUseBetterCombatWeapon(recruit, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                bettermobcombat$performBetterCombatAttack(recruit, wa, currentAttack);
                return true; // cancel as long as there is a current BC attack that the mob can perform
            }
            return false;
        });
    }
}
