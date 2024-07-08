package me.Thelnfamous1.bettermobcombat.mixin.compat.recruits;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import me.Thelnfamous1.bettermobcombat.mixin.MobMixin_AttackLogic;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(value = AbstractRecruitEntity.class, remap = false)
public abstract class AbstractRecruitEntityMixin extends MobMixin_AttackLogic {

    protected AbstractRecruitEntityMixin(EntityType<? extends LivingEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    @ModifyArg(
            method = {"doHurtTarget"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"
            ),
            index = 0
    )
    private ItemStack modify_getDamageBonus_doHurtTarget(ItemStack heldItem) {
        return this.bettermobcombat$getAlternateMainhandItem(heldItem);
    }

    @WrapOperation(
            method = {"doHurtTarget"},
            remap = true,
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/talhanation/recruits/entities/AbstractRecruitEntity;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    private ItemStack wrap_getMainHandItem_doHurtTarget(AbstractRecruitEntity instance, Operation<ItemStack> original) {
        return this.bettermobcombat$getAlternateMainHandItem(instance, original);
    }
}
