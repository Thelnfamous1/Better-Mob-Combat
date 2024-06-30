package me.Thelnfamous1.bettermobcombat.mixin.client;

import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.IMutableModel;
import dev.kosmx.playerAnim.impl.IPlayerModel;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import me.Thelnfamous1.bettermobcombat.client.MobModelHelper;
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
        extends AgeableListModel<T> implements IPlayerModel {


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
        if(!PlayerModel.class.isInstance(this)){
            IMutableModel thisWithMixin = (IMutableModel) this;
            bettermobcombat$emoteSupplier.set(null);

            thisWithMixin.setEmoteSupplier(bettermobcombat$emoteSupplier);
        }
    }

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void setEmote(T mob, float $$1, float $$2, float $$3, float $$4, float $$5, CallbackInfo ci){
        if(!PlayerModel.class.isInstance(this)){
            if(!bettermobcombat$firstPersonNext && MobModelHelper.isAnimating(mob)){
                AnimationApplier emote = MobModelHelper.getAnimation(mob);
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
                MobModelHelper.resetBend(this.body);
                MobModelHelper.resetBend(this.leftArm);
                MobModelHelper.resetBend(this.rightArm);
                MobModelHelper.resetBend(this.leftLeg);
                MobModelHelper.resetBend(this.rightLeg);
            }
        }
    }

    @Override
    public void playerAnimator_prepForFirstPersonRender() {
        bettermobcombat$firstPersonNext = true;
    }
}
