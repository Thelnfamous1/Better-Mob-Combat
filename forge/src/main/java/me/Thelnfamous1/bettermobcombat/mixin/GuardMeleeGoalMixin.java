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
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tallestegg.guardvillagers.entities.Guard;

@Pseudo
@Mixin(value = Guard.GuardMeleeGoal.class, remap = false)
public abstract class GuardMeleeGoalMixin extends MeleeAttackGoal {

    @Shadow @Final public Guard guard;

    public GuardMeleeGoalMixin(PathfinderMob p_25552_, double p_25553_, boolean p_25554_) {
        super(p_25552_, p_25553_, p_25554_);
    }

    @Inject(
            method = "checkAndPerformAttack", remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity $$0, double $$1, CallbackInfo ci) {
        MobCombatHelper.onHoldingAnimatedAttackWeapon(this.guard, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)this.guard).getCurrentAttack();
            if(currentAttack != null){
                ci.cancel();
                if(((MobAttackWindup)m).bettermobcombat$getAttackCooldown() >= 0 && MobCombatHelper.isWithinAttackRange(m, this.guard.getTarget(), currentAttack.attack(), wa.attackRange())){
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    this.resetAttackCooldown();
                    // Guard specific attack handling
                    this.guard.stopUsingItem();
                    if (this.guard.shieldCoolDown == 0) {
                        this.guard.shieldCoolDown = 8;
                    }
                }
            }
        });
    }

}
