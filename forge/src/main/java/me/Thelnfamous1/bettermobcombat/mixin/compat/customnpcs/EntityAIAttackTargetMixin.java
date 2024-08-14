package me.Thelnfamous1.bettermobcombat.mixin.compat.customnpcs;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import noppes.npcs.ai.EntityAIAttackTarget;
import noppes.npcs.entity.EntityNPCInterface;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(EntityAIAttackTarget.class)
public class EntityAIAttackTargetMixin {

    @Shadow(remap = false) private EntityNPCInterface npc;

    @Shadow(remap = false) private LivingEntity entityTarget;

    @Shadow(remap = false) private int attackTick;

    @WrapOperation(method = "canUse", at = @At(value = "INVOKE", target = "Lnoppes/npcs/entity/EntityNPCInterface;isInRange(Lnet/minecraft/world/entity/Entity;D)Z", remap = false, ordinal = 0))
    private boolean wrap_isInRange_canUse(EntityNPCInterface instance, Entity entity, double range, Operation<Boolean> original){
        return bettermobcombat$wrappedInMeleeRangeCheck(instance, entity, range, original);
    }

    @Unique
    private static Boolean bettermobcombat$wrappedInMeleeRangeCheck(EntityNPCInterface attacker, Entity target, double range, Operation<Boolean> original) {
        //TODO: Implement once CustomNPC compat is available via Mob Player Animator
        /*
        return MobCombatHelper.applyWithBetterCombatWeapon(attacker, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                return MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange());
            }
            return false;
        }, () -> original.call(attacker, target, range));
         */
        return original.call(attacker, target, range);
    }

    @WrapOperation(method = "canContinueToUse", at = @At(value = "INVOKE", target = "Lnoppes/npcs/entity/EntityNPCInterface;isInRange(Lnet/minecraft/world/entity/Entity;D)Z", remap = false, ordinal = 1))
    private boolean wrap_isInRange_canContinueToUse(EntityNPCInterface instance, Entity entity, double range, Operation<Boolean> original){
        return bettermobcombat$wrappedInMeleeRangeCheck(instance, entity, range, original);
    }

    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnoppes/npcs/ai/EntityAIAttackTarget;attackTick:I", remap = false, opcode = Opcodes.PUTFIELD, ordinal = 0, shift = At.Shift.AFTER), cancellable = true)
    private void handleBetterCombatAttack(CallbackInfo ci){
        //TODO: Implement once CustomNPC compat is available via Mob Player Animator
        /*
        MobCombatHelper.onHoldingBetterCombatWeapon(this.npc, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat) m).getCurrentAttack();
            if (currentAttack != null) {
                if (MobCombatHelper.isAttackReady(m) && MobCombatHelper.isWithinAttackRange(m, this.entityTarget, currentAttack.attack(), wa.attackRange())) {
                    ((MobAttackWindup) m).bettermobcombat$startUpswing(wa);
                    this.attackTick = ((MobAttackWindup) m).bettermobcombat$getAttackCooldown();
                }
                ci.cancel();
            }
        });
         */
    }
}
