package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HumanoidMobRenderer.class)
public abstract class HumanoidMobRendererMixin<T extends Mob, M extends HumanoidModel<T>> extends MobRendererMixin<T, M>{

    protected HumanoidMobRendererMixin(EntityRendererProvider.Context $$0) {
        super($$0);
    }

    @Override
    protected void bettermobcombat$handleFirstPersonRender(T mob) {
        if (FirstPersonMode.isFirstPersonPass()) {
            var animationApplier = ((IAnimatedPlayer) mob).playerAnimator_getAnimation();
            var config = animationApplier.getFirstPersonConfiguration();

            if (mob == Minecraft.getInstance().getCameraEntity()) {
                // Hiding all parts, because they should not be visible in first person
                setAllPartsVisible(false);
                // Showing arms based on configuration
                var showRightArm = config.isShowRightArm();
                var showLeftArm = config.isShowLeftArm();
                this.model.rightArm.visible = showRightArm;
                this.model.leftArm.visible = showLeftArm;
            }
        }

        // No `else` case needed to show parts, since the default state should be correct already
    }

    @Unique
    private void setAllPartsVisible(boolean visible) {
        this.model.head.visible = visible;
        this.model.body.visible = visible;
        this.model.leftLeg.visible = visible;
        this.model.rightLeg.visible = visible;
        this.model.rightArm.visible = visible;
        this.model.leftArm.visible = visible;

        this.model.hat.visible = visible;
    }

    @Override
    protected void bettermobcombat$handleAnimationTick(T entity, PoseStack matrixStack, float tickDelta) {
        var animationPlayer = ((IAnimatedPlayer) entity).playerAnimator_getAnimation();
        animationPlayer.setTickDelta(tickDelta);
        if(animationPlayer.isActive()){

            //These are additive properties
            Vec3f position = animationPlayer.get3DTransform("body", TransformType.POSITION, Vec3f.ZERO);
            matrixStack.translate(position.getX(), position.getY() + 0.7, position.getZ());
            Vec3f rotation = animationPlayer.get3DTransform("body", TransformType.ROTATION, Vec3f.ZERO);
            matrixStack.mulPose(Axis.ZP.rotation(rotation.getZ()));    //roll
            matrixStack.mulPose(Axis.YP.rotation(rotation.getY()));    //pitch
            matrixStack.mulPose(Axis.XP.rotation(rotation.getX()));    //yaw
            matrixStack.translate(0, - 0.7d, 0);
        }
    }
}
