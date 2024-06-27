package me.Thelnfamous1.bettermobcombat.mixin.client;

import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import me.Thelnfamous1.bettermobcombat.api.client.MobPlayerModel;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Function;

@Mixin(value = HumanoidModel.class, priority = 2000)//Apply after NotEnoughAnimation's inject
public abstract class HumanoidModelMixin<T extends LivingEntity>
        extends AgeableListModel<T>
        implements ArmedModel, HeadedModel, MobPlayerModel {


    @Shadow @Final public ModelPart leftLeg;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart hat;
    @Unique
    protected final SetableSupplier<AnimationProcessor> bettermobcombat$emoteSupplier = new SetableSupplier<>();
    @Unique
    protected boolean bettermobcombat$firstPersonNext = false;

    @Inject(method = "<init>(Lnet/minecraft/client/model/geom/ModelPart;Ljava/util/function/Function;)V", at = @At("RETURN"))
    private void initBendableStuff(ModelPart $$0, Function $$1, CallbackInfo ci){
        if(!this.bettermobcombat$isPlayerModel()){
            IMutableModel thisWithMixin = (IMutableModel) this;
            bettermobcombat$emoteSupplier.set(null);

            thisWithMixin.setEmoteSupplier(bettermobcombat$emoteSupplier);
        }
    }

    @Unique
    private boolean bettermobcombat$isPlayerModel(){
        return PlayerModel.class.isInstance(this);
    }

    @Unique
    private void bettermobcombat$setDefaultPivot(){
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

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At(value = "HEAD"))
    private void setDefaultBeforeRender(T $$0, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        /*
        if(!this.bettermobcombat$isPlayerModel()){
            bettermobcombat$setDefaultPivot(); //to not make everything wrong
        }
         */
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void setEmote(T zombie, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        if(!this.bettermobcombat$isPlayerModel()){
            if(!bettermobcombat$firstPersonNext && ((IAnimatedPlayer)zombie).playerAnimator_getAnimation().isActive()){
                AnimationApplier emote = ((IAnimatedPlayer) zombie).playerAnimator_getAnimation();
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

    @Override
    public void playerAnimator_prepForFirstPersonRender() {
        bettermobcombat$firstPersonNext = true;
    }
}
