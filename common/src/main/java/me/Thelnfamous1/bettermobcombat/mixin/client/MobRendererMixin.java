package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
}
