package me.Thelnfamous1.bettermobcombat.api.client;

import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IPlayerModel;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;

public interface MobPlayerModel extends IPlayerModel {
    static void bettermobcombat$resetBend(ModelPart part) {
        IBendHelper.INSTANCE.bend(part, null);
    }

    static boolean bettermobcombat$isAnimating(LivingEntity mob){
        return ((IAnimatedPlayer) mob).playerAnimator_getAnimation().isActive();
    }
}
