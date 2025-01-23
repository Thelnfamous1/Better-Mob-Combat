package me.Thelnfamous1.bettermobcombat.mixin.compat.minecolonies;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.core.entity.ai.workers.guard.KnightCombatAI;
import com.minecolonies.core.entity.citizen.EntityCitizen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = KnightCombatAI.class, remap = false)
public abstract class KnightCombatAIMixin extends AttackMoveAIMixin{

    public KnightCombatAIMixin(Mob user, int targetFrequency, ITickRateStateMachine stateMachine) {
        super(user, targetFrequency, stateMachine);
    }

    @WrapWithCondition(method = "doAttack", at = @At(value = "INVOKE", target = "Lcom/minecolonies/core/entity/citizen/EntityCitizen;swing(Lnet/minecraft/world/InteractionHand;)V", remap = true))
    private boolean wrap_swing(EntityCitizen instance, InteractionHand hand){
        return !this.bettermobcombat$betterCombatAttack;
    }

    @WrapWithCondition(method = "doAttack", at = @At(value = "INVOKE", target = "Lcom/minecolonies/core/entity/ai/workers/guard/KnightCombatAI;doAoeAttack(Lnet/minecraft/world/damagesource/DamageSource;D)V"))
    private boolean wrap_doAoeAttack(KnightCombatAI instance, DamageSource entities, double d0){
        return !this.bettermobcombat$betterCombatAttack;
    }
}
