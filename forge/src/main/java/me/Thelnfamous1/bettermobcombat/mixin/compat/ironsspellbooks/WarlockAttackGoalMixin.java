package me.Thelnfamous1.bettermobcombat.mixin.compat.ironsspellbooks;

import io.redspace.ironsspellbooks.api.entity.IMagicEntity;
import io.redspace.ironsspellbooks.entity.mobs.goals.WarlockAttackGoal;
import io.redspace.ironsspellbooks.entity.mobs.goals.WizardAttackGoal;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = WarlockAttackGoal.class, remap = false)
public abstract class WarlockAttackGoalMixin extends WizardAttackGoal {

    @Shadow protected boolean wantsToMelee;

    public WarlockAttackGoalMixin(IMagicEntity abstractSpellCastingMob, double pSpeedModifier, int pAttackInterval) {
        super(abstractSpellCastingMob, pSpeedModifier, pAttackInterval);
    }

    @Inject(method = "handleAttackLogic", at = @At("HEAD"), cancellable = true)
    private void pre_handleAttackLogic(double distanceSquared, CallbackInfo ci){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.mob, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                if (this.wantsToMelee && MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange()) && !this.spellCastingMob.isCasting()) {
                    if(MobCombatHelper.isAttackReady(m)){
                        MobCombatHelper.setDelayedUpswing(m, () -> {
                            ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                            this.attackTime = (((MobAttackWindup) m).bettermobcombat$getAttackCooldown());
                        });
                    }
                } else{
                    super.handleAttackLogic(distanceSquared);
                }
                ci.cancel(); // cancel as long as there is a current BC attack that the mob can perform
            }
        });
    }
}
