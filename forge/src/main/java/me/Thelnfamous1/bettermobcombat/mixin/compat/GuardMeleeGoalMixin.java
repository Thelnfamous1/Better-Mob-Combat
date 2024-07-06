package me.Thelnfamous1.bettermobcombat.mixin.compat;

import me.Thelnfamous1.bettermobcombat.mixin.MeleeAttackGoalMixin;
import net.minecraft.world.entity.LivingEntity;
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
public abstract class GuardMeleeGoalMixin extends MeleeAttackGoalMixin {

    @Shadow @Final public Guard guard;

    @Override
    protected void bettermobcombat$postBetterCombatAttack() {
        // Guard specific post attack handling
        this.guard.stopUsingItem();
        if (this.guard.shieldCoolDown == 0) {
            this.guard.shieldCoolDown = 8;
        }
    }

    // backup, should almost always never be called
    @Inject(
            method = "checkAndPerformAttack", remap = true,
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity $$0, double $$1, CallbackInfo ci) {
        if(this.bettermobcombat$useBetterCombatAttackCheck()){
            ci.cancel();
        }
    }

}
