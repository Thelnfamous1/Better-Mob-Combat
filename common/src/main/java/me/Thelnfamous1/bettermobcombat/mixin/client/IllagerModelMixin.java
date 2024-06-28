package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.google.common.collect.ImmutableList;
import com.llamalad7.mixinextras.injector.WrapWithCondition;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.Helper;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.IUpperPartHelper;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import me.Thelnfamous1.bettermobcombat.api.client.MobPlayerModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = IllagerModel.class, priority = 2000)
public abstract class IllagerModelMixin<T extends AbstractIllager> extends HierarchicalModelMixin<T> implements MobPlayerModel, IMutableModel {
    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart hat;
    @Shadow @Final private ModelPart leftArm;
    @Shadow @Final private ModelPart rightArm;
    @Shadow @Final private ModelPart rightLeg;
    @Shadow @Final private ModelPart leftLeg;
    @Unique
    private SetableSupplier<AnimationProcessor> bettermobcombat$animation = new SetableSupplier<>();
    @Unique
    private final SetableSupplier<AnimationProcessor> bettermobcombat$emoteSupplier = new SetableSupplier<>();
    @Unique
    private boolean bettermobcombat$firstPersonNext = false;
    @Unique
    private ModelPart bettermobcombat$body;

    public IllagerModelMixin(Function<ResourceLocation, RenderType> $$0) {
        super($$0);
    }

    @Override
    public void setEmoteSupplier(SetableSupplier<AnimationProcessor> emoteSupplier) {
        this.bettermobcombat$animation = emoteSupplier;
    }

    @Override
    public SetableSupplier<AnimationProcessor> getEmoteSupplier(){
        return this.bettermobcombat$animation;
    }

    @Override
    protected boolean bettermobcombat$bendAnimation(PoseStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        if(Helper.isBendEnabled() && this.bettermobcombat$animation.get() != null && this.bettermobcombat$animation.get().isActive()){
            this.bettermobcombat$headParts().forEach((part)->{
                if(! ((IUpperPartHelper)(Object)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            this.bettermobcombat$bodyParts().forEach((part)->{
                if(! ((IUpperPartHelper)(Object)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });

            SetableSupplier<AnimationProcessor> emoteSupplier = this.bettermobcombat$animation;
            matrices.pushPose();
            IBendHelper.rotateMatrixStack(matrices, emoteSupplier.get().getBend("body"));
            this.bettermobcombat$headParts().forEach((part)->{
                if(((IUpperPartHelper)(Object)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            this.bettermobcombat$bodyParts().forEach((part)->{
                if(((IUpperPartHelper)(Object)part).isUpperPart()){
                    part.render(matrices, vertices, light, overlay, red, green, blue, alpha);
                }
            });
            matrices.popPose();
            return true;
        }
        return false;
    }

    @Unique
    protected Iterable<ModelPart> bettermobcombat$headParts() {
        return ImmutableList.of(this.head);
    }

    @Unique
    protected Iterable<ModelPart> bettermobcombat$bodyParts() {
        return ImmutableList.of(this.bettermobcombat$body, this.rightArm, this.leftArm, this.rightLeg, this.leftLeg, this.hat);
    }

    @Override
    protected void bettermobcombat$copyMutatedAttributes(EntityModel<T> otherModel) {
        if(this.bettermobcombat$animation != null) {
            ((IMutableModel) otherModel).setEmoteSupplier(this.bettermobcombat$animation);
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void initBendableStuff(ModelPart root, CallbackInfo ci){
        // Copied from PlayerAnimator's BipedEntityModelMixin#initBend
        IBendHelper.INSTANCE.initBend(root.getChild("body"), Direction.DOWN);
        IBendHelper.INSTANCE.initBend(root.getChild("right_arm"), Direction.UP);
        IBendHelper.INSTANCE.initBend(root.getChild("left_arm"), Direction.UP);
        IBendHelper.INSTANCE.initBend(root.getChild("right_leg"), Direction.UP);
        IBendHelper.INSTANCE.initBend(root.getChild("left_leg"), Direction.UP);
        ((IUpperPartHelper)(Object)rightArm).setUpperPart(true);
        ((IUpperPartHelper)(Object)leftArm).setUpperPart(true);
        ((IUpperPartHelper)(Object)head).setUpperPart(true);
        ((IUpperPartHelper)(Object)hat).setUpperPart(true);

        // Copied from PlayerAnimator's PlayerModelMixin#initBendableStuff
        bettermobcombat$emoteSupplier.set(null);

        this.setEmoteSupplier(bettermobcombat$emoteSupplier);

        // IllagerModel does not store the "body" ModelPart as a field
        this.bettermobcombat$body = root.getChild("body");
    }

    @WrapWithCondition(method = "setupAnim",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V")
    )
    private boolean onlyAnimateZombieArmsIfAllowed(ModelPart leftArm, ModelPart rightArm, boolean aggressive, float attackTime, float bob,
                                                   T illager,
                                                   float $$1,
                                                   float $$2,
                                                   float $$3,
                                                   float $$4,
                                                   float $$5) {
        return !MobPlayerModel.bettermobcombat$isAnimating(illager);
    }

    @WrapWithCondition(method = "setupAnim",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;swingWeaponDown(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/world/entity/Mob;FF)V")
    )
    private boolean onlyAnimateWeaponSwingIfAllowed(ModelPart rightArm, ModelPart leftArm, Mob mob, float attackTime, float bob) {
        return !MobPlayerModel.bettermobcombat$isAnimating(mob);
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", at = @At("TAIL"))
    private void setEmote(T illager, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        if(!bettermobcombat$firstPersonNext && ((IAnimatedPlayer)illager).playerAnimator_getAnimation().isActive()){
            AnimationApplier emote = ((IAnimatedPlayer) illager).playerAnimator_getAnimation();
            bettermobcombat$emoteSupplier.set(emote);

            emote.updatePart("head", this.head);
            this.hat.copyFrom(this.head);

            emote.updatePart("leftArm", this.leftArm);
            emote.updatePart("rightArm", this.rightArm);
            emote.updatePart("leftLeg", this.leftLeg);
            emote.updatePart("rightLeg", this.rightLeg);
            emote.updatePart("torso", this.bettermobcombat$body);


        }
        else {
            bettermobcombat$firstPersonNext = false;
            bettermobcombat$emoteSupplier.set(null);
            MobPlayerModel.bettermobcombat$resetBend(this.bettermobcombat$body);
            MobPlayerModel.bettermobcombat$resetBend(this.leftArm);
            MobPlayerModel.bettermobcombat$resetBend(this.rightArm);
            MobPlayerModel.bettermobcombat$resetBend(this.leftLeg);
            MobPlayerModel.bettermobcombat$resetBend(this.rightLeg);
        }
    }

    @Override
    public void playerAnimator_prepForFirstPersonRender() {
        bettermobcombat$firstPersonNext = true;
    }
}
