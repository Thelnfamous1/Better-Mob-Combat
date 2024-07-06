package me.Thelnfamous1.bettermobcombat.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.Thelnfamous1.bettermobcombat.BetterMobCombat;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin({ProjectileWeaponItem.class})
public class RangedWeaponItemMixin {
    public RangedWeaponItemMixin() {
    }

    @WrapOperation(
        method = {"getHeldProjectile"},
        require = 0,
        at = {@At(
    value = "INVOKE",
    target = "Lnet/minecraft/world/entity/LivingEntity;getItemInHand(Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/item/ItemStack;"
)}
    )
    private static ItemStack getHeldProjectile_Wrapped_BetterMobCombat(LivingEntity entity, InteractionHand hand, Operation<ItemStack> original) {
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(entity)){
            return original.call(entity, hand);
        }
        if (entity instanceof Mob mob) {
            return MobCombatHelper.getDirectOffhand(mob);
        } else {
            return original.call(entity, hand);
        }
    }
}