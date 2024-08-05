package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.Thelnfamous1.bettermobcombat.api.MobAttackAnimation;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.monster.SpellcasterIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = "net/minecraft/client/renderer/entity/EvokerRenderer$1")
public abstract class ItemInHandLayerMixin_EvokerRenderer extends ItemInHandLayer<SpellcasterIllager, IllagerModel<SpellcasterIllager>> {

    public ItemInHandLayerMixin_EvokerRenderer(RenderLayerParent<SpellcasterIllager, IllagerModel<SpellcasterIllager>> $$0, ItemInHandRenderer $$1) {
        super($$0, $$1);
    }

    @WrapOperation(method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/monster/SpellcasterIllager;FFFFFF)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/monster/SpellcasterIllager;isCastingSpell()Z"))
    private boolean wrap_isCastingSpellForRenderingItemInHand(SpellcasterIllager instance, Operation<Boolean> original){
        if(((MobAttackAnimation)instance).bettermobcombat$hasActiveAttackAnimation()){
            return true;
        }
        return original.call(instance);
    }
}
