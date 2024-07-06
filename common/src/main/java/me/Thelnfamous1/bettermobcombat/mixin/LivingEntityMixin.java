package me.Thelnfamous1.bettermobcombat.mixin;

import me.Thelnfamous1.bettermobcombat.api.MobAttackStrength;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {

    @Inject(
            method = {"getAttributeValue(Lnet/minecraft/world/entity/ai/attributes/Attribute;)D"},
            at = {@At("HEAD")},
            cancellable = true
    )
    public void getAttributeValue_Inject(Attribute attribute, CallbackInfoReturnable<Double> cir) {
        if ((Object)this instanceof Mob mob) {
            int comboCount = ((PlayerAttackProperties)mob).getComboCount();
            if (!mob.level().isClientSide && comboCount > 0 && MobAttackHelper.shouldAttackWithOffHand(mob, comboCount)) {
                MobAttackHelper.offhandAttributes(mob, () -> {
                    double value = mob.getAttributes().getValue(attribute);
                    cir.setReturnValue(value);
                });
                cir.cancel();
            }
        }
    }

    @Inject(method = "swing(Lnet/minecraft/world/InteractionHand;)V", at = @At("TAIL"))
    private void handleSwing(InteractionHand $$0, CallbackInfo ci){
        if(this instanceof MobAttackStrength mob){
            mob.bettercombat$resetAttackStrengthTicker();
        }
    }
}
