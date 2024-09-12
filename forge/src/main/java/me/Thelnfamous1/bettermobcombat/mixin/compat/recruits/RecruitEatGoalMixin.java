package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.entities.ai.RecruitEatGoal;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(RecruitEatGoal.class)
public class RecruitEatGoalMixin {

    @Shadow public AbstractRecruitEntity recruit;

    @Inject(method = "canUse", at = @At("RETURN"), cancellable = true)
    private void pre_canUse(CallbackInfoReturnable<Boolean> cir){
        if(cir.getReturnValue() && MobAttackHelper.isTwoHandedWielding(this.recruit)){
            cir.setReturnValue(false);
        }
    }
}
