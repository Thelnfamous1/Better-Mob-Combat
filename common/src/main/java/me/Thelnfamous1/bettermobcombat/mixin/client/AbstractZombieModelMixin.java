package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.IPlayerModel;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import net.minecraft.client.model.AbstractZombieModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Monster;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AbstractZombieModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public abstract class AbstractZombieModelMixin<T extends Monster> extends HumanoidModel<T> implements IPlayerModel {
    @Unique
    private final SetableSupplier<AnimationProcessor> emoteSupplier = new SetableSupplier<>();
    @Unique
    private boolean firstPersonNext = false;

    public AbstractZombieModelMixin(ModelPart $$0) {
        super($$0);
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(ModelPart $$0, CallbackInfo ci){
        IMutableModel thisWithMixin = (IMutableModel) this;
        emoteSupplier.set(null);

        thisWithMixin.setEmoteSupplier(emoteSupplier);
    }

    @Unique
    private void setDefaultPivot(){
        this.leftLeg.setPos(1.9F, 12.0F, 0.0F);
        this.rightLeg.setPos(- 1.9F, 12.0F, 0.0F);
        this.head.setPos(0.0F, 0.0F, 0.0F);
        this.rightArm.z = 0.0F;
        this.rightArm.x = - 5.0F;
        this.leftArm.z = 0.0F;
        this.leftArm.x = 5.0F;
        this.body.xRot = 0.0F;
        this.rightLeg.z = 0.1F;
        this.leftLeg.z = 0.1F;
        this.rightLeg.y = 12.0F;
        this.leftLeg.y = 12.0F;
        this.head.y = 0.0F;
        this.head.zRot = 0f;
        this.body.y = 0.0F;
        this.body.x = 0f;
        this.body.z = 0f;
        this.body.yRot = 0;
        this.body.zRot = 0;
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        setDefaultPivot(); //to not make everything wrong
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/Monster;FFFFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", ordinal = 0, shift = At.Shift.AFTER))
    private void setEmote(T zombie, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        if(!firstPersonNext && ((IAnimatedPlayer)zombie).playerAnimator_getAnimation().isActive()){
            AnimationApplier emote = ((IAnimatedPlayer) zombie).playerAnimator_getAnimation();
            emoteSupplier.set(emote);

            emote.updatePart("head", this.head);
            this.hat.copyFrom(this.head);

            emote.updatePart("leftArm", this.leftArm);
            emote.updatePart("rightArm", this.rightArm);
            emote.updatePart("leftLeg", this.leftLeg);
            emote.updatePart("rightLeg", this.rightLeg);
            emote.updatePart("torso", this.body);


        }
        else {
            firstPersonNext = false;
            emoteSupplier.set(null);
            resetBend(this.body);
            resetBend(this.leftArm);
            resetBend(this.rightArm);
            resetBend(this.leftLeg);
            resetBend(this.rightLeg);
        }
    }

    @Unique
    private static void resetBend(ModelPart part) {
        IBendHelper.INSTANCE.bend(part, null);
    }

    @Override
    public void playerAnimator_prepForFirstPersonRender() {
        firstPersonNext = true;
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
