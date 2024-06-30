package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import me.Thelnfamous1.bettermobcombat.client.MobModelHelper;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobRenderer.class)
public abstract class MobRendererMixin<T extends Mob, M extends EntityModel<T>> extends LivingEntityRendererMixin<T, M> {

    protected MobRendererMixin(EntityRendererProvider.Context $$0) {
        super($$0);
    }

    @Inject(method = "render(Lnet/minecraft/world/entity/Mob;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At("HEAD"))
    private void pre_render(T mob, float $$1, float $$2, PoseStack $$3, MultiBufferSource $$4, int $$5, CallbackInfo ci){
        this.bettermobcombat$handleFirstPersonRender(mob);
    }

    @Unique
    protected void bettermobcombat$handleFirstPersonRender(T mob) {
    }

    @Override
    protected void bettermobcombat$applyBodyRotations(Mob mob, PoseStack matrixStack, float tickDelta) {
        AnimationApplier animationPlayer = MobModelHelper.getAnimation(mob);
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
