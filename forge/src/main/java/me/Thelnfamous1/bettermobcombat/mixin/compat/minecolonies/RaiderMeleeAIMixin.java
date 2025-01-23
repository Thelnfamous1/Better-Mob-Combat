package me.Thelnfamous1.bettermobcombat.mixin.compat.minecolonies;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.minecolonies.api.entity.ai.statemachine.tickratestatemachine.ITickRateStateMachine;
import com.minecolonies.api.entity.mobs.AbstractEntityMinecoloniesMonster;
import com.minecolonies.core.entity.mobs.aitasks.RaiderMeleeAI;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;

@Pseudo
@Mixin(value = RaiderMeleeAI.class, remap = false)
public abstract class RaiderMeleeAIMixin extends AttackMoveAIMixin{

    public RaiderMeleeAIMixin(Mob user, int targetFrequency, ITickRateStateMachine stateMachine) {
        super(user, targetFrequency, stateMachine);
    }

    @WrapWithCondition(method = "doAttack", at = @At(value = "INVOKE", target = "Lcom/minecolonies/api/entity/mobs/AbstractEntityMinecoloniesMonster;swing(Lnet/minecraft/world/InteractionHand;)V", remap = true))
    private boolean wrap_swing(AbstractEntityMinecoloniesMonster instance, InteractionHand hand){
        return !this.bettermobcombat$betterCombatAttack;
    }
}
