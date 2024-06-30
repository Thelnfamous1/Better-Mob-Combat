package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<
        T extends LivingEntity,
        M extends EntityModel<T>
        >
        extends EntityRenderer<T>
        implements RenderLayerParent<T, M> {
    @Shadow protected M model;

    protected LivingEntityRendererMixin(EntityRendererProvider.Context $$0) {
        super($$0);
    }

    @Inject(method = "setupRotations", at = @At("RETURN"))
    private void post_setupRotations(T entity, PoseStack matrixStack, float $$2, float $$3, float tickDelta, CallbackInfo ci){
        if(entity instanceof Mob mob){
            this.bettermobcombat$applyBodyRotations(mob, matrixStack, tickDelta);
        }
    }

    @Unique
    protected void bettermobcombat$applyBodyRotations(Mob entity, PoseStack matrixStack, float tickDelta) {
    }
}
