package me.Thelnfamous1.bettermobcombat.mixin.client;

import me.Thelnfamous1.bettermobcombat.api.client.MobPlayerModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.RangedAttackMob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = SkeletonModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public abstract class SkeletonModelMixin<T extends Mob & RangedAttackMob> extends HumanoidModel<T>{

    public SkeletonModelMixin(ModelPart $$0) {
        super($$0);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER), cancellable = true)
    private void onlyAnimateSkeletonArmsIfAllowed(T skeleton, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        if(MobPlayerModel.bettermobcombat$isAnimating(skeleton)){
            ci.cancel();
        }
    }

}
