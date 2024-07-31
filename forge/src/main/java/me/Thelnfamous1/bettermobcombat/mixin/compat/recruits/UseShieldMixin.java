package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.talhanation.recruits.entities.ai.UseShield;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.PathfinderMob;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = UseShield.class, remap = false)
public class UseShieldMixin {

    @Shadow @Final public PathfinderMob entity;

    @Inject(method = "canRaiseShield", at = @At(value = "RETURN", ordinal = 1), cancellable = true)
    private void provideAlternateDistanceCheck(CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(
                this.entity,
                (m, wa) -> {
                    AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
                    if (currentAttack != null) {
                        // 1.5 is what the recruit's attack reach is multiplied by before checking if its distance to the target is greater than it
                        cir.setReturnValue(!MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange() * 1.5F));
                    }
                });
    }

}
