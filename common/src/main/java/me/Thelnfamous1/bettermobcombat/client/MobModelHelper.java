package me.Thelnfamous1.bettermobcombat.client;

import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public class MobModelHelper {
    public static void resetBend(ModelPart part) {
        IBendHelper.INSTANCE.bend(part, null);
    }

    public static AnimationApplier getAnimation(LivingEntity mob){
        return ((IAnimatedPlayer) mob).playerAnimator_getAnimation();
    }

    public static boolean isAnimating(LivingEntity mob){
        return getAnimation(mob).isActive();
    }
}
