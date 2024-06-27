package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import me.Thelnfamous1.bettermobcombat.api.client.MobPlayerModel;
import net.minecraft.client.model.PiglinModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PiglinModel.class)
public abstract class PiglinModelMixin<T extends Mob> extends HumanoidModelMixin<T> {

    @WrapWithCondition(method = "setupAnim",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/PiglinModel;holdWeaponHigh(Lnet/minecraft/world/entity/Mob;)V")
    )
    private boolean onlyAnimateWeaponHighIfAllowed(PiglinModel<T> model, T piglin) {
        return !MobPlayerModel.bettermobcombat$isAnimating(piglin);
    }

    @WrapWithCondition(method = "setupAttackAnimation",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;swingWeaponDown(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/world/entity/Mob;FF)V")
    )
    private boolean onlyAnimateWeaponSwingIfAllowed(ModelPart rightArm, ModelPart leftArm, Mob mob, float attackTime, float bob) {
        return !MobPlayerModel.bettermobcombat$isAnimating(mob);
    }

    @WrapWithCondition(method = "setupAnim",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V")
    )
    private boolean onlyAnimateZombieArmsIfAllowed(ModelPart leftArm, ModelPart rightArm, boolean aggressive, float attackTime, float bob,
                                                   T piglin,
                                                   float $$1,
                                                   float $$2,
                                                   float $$3,
                                                   float $$4,
                                                   float $$5) {
        return !MobPlayerModel.bettermobcombat$isAnimating(piglin);
    }

    @WrapWithCondition(method = "setupAnim", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/geom/ModelPart;loadPose(Lnet/minecraft/client/model/geom/PartPose;)V"))
    private boolean onlyLoadPoseIfAllowed(ModelPart part, PartPose pose,
                                       T piglin,
                                       float $$1,
                                       float $$2,
                                       float $$3,
                                       float $$4,
                                       float $$5){
        return !MobPlayerModel.bettermobcombat$isAnimating(piglin);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/Mob;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/PlayerModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", shift = At.Shift.AFTER))
    private void setEmote(T piglin, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        if(!bettermobcombat$firstPersonNext && ((IAnimatedPlayer)piglin).playerAnimator_getAnimation().isActive()){
            AnimationApplier emote = ((IAnimatedPlayer) piglin).playerAnimator_getAnimation();
            bettermobcombat$emoteSupplier.set(emote);

            emote.updatePart("head", this.head);
            this.hat.copyFrom(this.head);

            emote.updatePart("leftArm", this.leftArm);
            emote.updatePart("rightArm", this.rightArm);
            emote.updatePart("leftLeg", this.leftLeg);
            emote.updatePart("rightLeg", this.rightLeg);
            emote.updatePart("torso", this.body);


        }
        else {
            bettermobcombat$firstPersonNext = false;
            bettermobcombat$emoteSupplier.set(null);
            MobPlayerModel.bettermobcombat$resetBend(this.body);
            MobPlayerModel.bettermobcombat$resetBend(this.leftArm);
            MobPlayerModel.bettermobcombat$resetBend(this.rightArm);
            MobPlayerModel.bettermobcombat$resetBend(this.leftLeg);
            MobPlayerModel.bettermobcombat$resetBend(this.rightLeg);
        }
    }
}
