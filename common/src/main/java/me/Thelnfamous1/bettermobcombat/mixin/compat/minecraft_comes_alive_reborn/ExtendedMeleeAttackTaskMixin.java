package me.Thelnfamous1.bettermobcombat.mixin.compat.minecraft_comes_alive_reborn;

import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {"forge/net/mca/entity/ai/brain/tasks/ExtendedMeleeAttackTask", "fabric/net/mca/entity/ai/brain/tasks/ExtendedMeleeAttackTask"})
public abstract class ExtendedMeleeAttackTaskMixin {

    @Shadow(remap = false) protected abstract LivingEntity getTarget(Mob mobEntity);

    @Inject(method = "withinRange", remap = false, at = @At("HEAD"), cancellable = true)
    private void pre_withinRange(LivingEntity attacker, LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        if(attacker instanceof Mob mob){
            MobCombatHelper.onHoldingBetterCombatWeapon(mob, (m, wa) -> {
                AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
                if(currentAttack != null){
                    cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange()));
                }
            });
        }
    }

    @Inject(method = {"run", "start"}, at= @At("HEAD"), cancellable = true)
    private void pre_run(ServerLevel serverWorld, Mob mobEntity, long l, CallbackInfo ci){
        MobCombatHelper.onHoldingBetterCombatWeapon(mobEntity, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                LivingEntity target = this.getTarget(mobEntity);
                if (MobCombatHelper.isAttackReady(m) && MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange())) {
                    BehaviorUtils.lookAtEntity(mobEntity, target);
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    mobEntity.getBrain().setMemoryWithExpiry(MemoryModuleType.ATTACK_COOLING_DOWN, true, ((MobAttackWindup) m).bettermobcombat$getAttackCooldown());
                }
                ci.cancel();
            }
        });
    }
}
