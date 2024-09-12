package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.Thelnfamous1.bettermobcombat.api.MobAttackAnimation;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.monster.Vindicator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/client/renderer/entity/VindicatorRenderer$1")
public abstract class ItemInHandLayerMixin_VindicatorRenderer extends ItemInHandLayer<Vindicator, IllagerModel<Vindicator>> {

    public ItemInHandLayerMixin_VindicatorRenderer(RenderLayerParent<Vindicator, IllagerModel<Vindicator>> $$0, ItemInHandRenderer $$1) {
        super($$0, $$1);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/monster/Vindicator;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/Vindicator;isAggressive()Z"))
    private boolean wrap_isAggressiveForRenderingItemInHand(Vindicator instance, Operation<Boolean> original){
        if(((MobAttackAnimation)instance).bettermobcombat$isCombatAnimationActive()){
            return true;
        }
        return original.call(instance);
    }
}
