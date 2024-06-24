package me.Thelnfamous1.bettermobcombat.mixin;

import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MeleeAttackGoal.class)
public class MeleeAttackGoalMixin {

    @Shadow @Final protected PathfinderMob mob;

    @Inject(
            method = {"checkAndPerformAttack"},
            at = {@At(value = "INVOKE", target = "Lnet/minecraft/world/entity/PathfinderMob;swing(Lnet/minecraft/world/InteractionHand;)V")},
            cancellable = true
    )
    private void pre_doHurtTarget(LivingEntity $$0, double $$1, CallbackInfo ci) {
        if(MobCombatHelper.doAttackWithWeapon(this.mob)){
            ci.cancel();
        }

    }

}
