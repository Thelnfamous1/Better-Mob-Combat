package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AbstractZombieModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public abstract class AbstractZombieModelMixin<T extends Monster> extends HumanoidModel<T>{

    public AbstractZombieModelMixin(ModelPart $$0) {
        super($$0);
    }

    @WrapWithCondition(method = "setupAnim",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V")
    )
    private boolean onlyAnimateZombieArmsIfAllowed(ModelPart leftArm, ModelPart rightArm, boolean aggressive, float attackTime, float bob,
                                                   T zombie,
                                                   float $$1,
                                                   float $$2,
                                                   float $$3,
                                                   float $$4,
                                                   float $$5) {
        return !((IAnimatedPlayer) zombie).playerAnimator_getAnimation().isActive();
    }
}
