package me.Thelnfamous1.bettermobcombat.mixin.compat.minecolonies;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.minecolonies.api.entity.ai.statemachine.states.IState;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.core.entity.ai.combat.AttackMoveAI;
import com.minecolonies.core.entity.ai.combat.TargetAI;
import com.minecolonies.core.entity.pathfinding.pathresults.PathResult;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = AttackMoveAI.class, remap = false)
public abstract class AttackMoveAIMixin extends TargetAI{

    @Shadow protected long nextAttackTime;

    @Shadow private int pathAttempts;
    @Unique
    protected boolean bettermobcombat$betterCombatAttack;

    @Shadow protected abstract void doAttack(LivingEntity target);

    @Shadow private PathResult targetPath;

    @Shadow protected abstract PathResult moveInAttackPosition(LivingEntity target);

    public AttackMoveAIMixin(Mob user, int targetFrequency, ITickRateStateMachine stateMachine) {
        super(user, targetFrequency, stateMachine);
    }

    @Inject(method = "move", at = @At(value = "FIELD", target = "Lcom/minecolonies/core/entity/ai/combat/AttackMoveAI;targetPath:Lcom/minecolonies/core/entity/pathfinding/pathresults/PathResult;", opcode = Opcodes.GETFIELD, ordinal = 3), cancellable = true)
    private void pre_recalculatePath(CallbackInfoReturnable<IState> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.user, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                if (this.targetPath == null || this.user.getNavigation().isDone() || this.targetPath.isDone() && this.targetPath.hasPath() && !MobCombatHelper.isWithinAttackRange(m, this.target, currentAttack.attack(), wa.attackRange())) {
                    this.targetPath = this.moveInAttackPosition(this.target);
                    ++this.pathAttempts;
                }
                cir.setReturnValue(null);
            }
        });
    }

    @Inject(method = "tryAttack", at = @At(value = "INVOKE", target = "Lcom/minecolonies/core/entity/ai/combat/AttackMoveAI;canAttack()Z", shift = At.Shift.AFTER), cancellable = true)
    private void pre_tryAttack(CallbackInfoReturnable<IState> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.user, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                if(MobCombatHelper.isAttackReady(m)
                        && MobCombatHelper.isWithinAttackRange(m, this.target, currentAttack.attack(), wa.attackRange())
                        && this.user.getSensing().hasLineOfSight(this.target)){
                    this.pathAttempts = 0;
                    this.user.getLookControl().setLookAt(this.target);
                    MobCombatHelper.setDelayedUpswing(m, () -> {
                        ((MobAttackWindup) m).bettermobcombat$startUpswing(wa, (atk, trg) -> {
                            this.bettermobcombat$betterCombatAttack = true;
                            if(trg instanceof LivingEntity livingTarget){
                                this.doAttack(livingTarget);
                            }
                            this.bettermobcombat$betterCombatAttack = false;
                        });
                        this.nextAttackTime = this.user.level().getGameTime() + ((MobAttackWindup) m).bettermobcombat$getAttackCooldown();
                    });
                    cir.setReturnValue(null);
                } else{
                    cir.setReturnValue(null);
                }
            }
        });
    }

    @WrapWithCondition(method = "doAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Mob;swing(Lnet/minecraft/world/InteractionHand;)V", remap = true))
    private boolean wrap_swing(Mob instance, InteractionHand hand){
        return !this.bettermobcombat$betterCombatAttack;
    }

    @Inject(method = "isInAttackDistance", at = @At("HEAD"), cancellable = true)
    private void pre_isInAttackDistance(LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon(this.user, (m, wa) -> {
            AttackHand currentAttack = ((EntityPlayer_BetterCombat)m).getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange()));
            }
        });
    }
}
