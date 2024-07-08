package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.talhanation.recruits.entities.HorsemanEntity;
import com.talhanation.recruits.entities.ai.HorsemanAttackAI;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(value = HorsemanAttackAI.class, remap = false)
public abstract class HorsemanAttackAIMixin extends Goal {

    @Shadow @Final private HorsemanEntity horseman;

    @Shadow private int ticksUntilNextAttack;

    // backup, should almost always never be called
    @Inject(
            method = "checkAndPerformAttack",
            remap = false,
            at = @At("HEAD"),
            cancellable = true
    )
    private void pre_checkAndPerformAttack(LivingEntity target, CallbackInfo ci) {
        if(this.bettermobcombat$useBetterCombatAttackCheck()){
            ci.cancel();
        }
    }

    @Unique
    protected boolean bettermobcombat$useBetterCombatAttackCheck() {
        return MobCombatHelper.canUseBetterCombatWeapon(this.horseman, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                if (MobCombatHelper.isAttackReady(m) && MobCombatHelper.isWithinAttackRange(m, m.getTarget(), currentAttack.attack(), wa.attackRange())) {
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
    protected void bettermobcombat$setTicksUntilNextAttack(int ticksUntilNextAttack){
        this.ticksUntilNextAttack = this.adjustedTickDelay(ticksUntilNextAttack);
    }

    @Unique
    protected void bettermobcombat$postBetterCombatAttack() {
    }
}
