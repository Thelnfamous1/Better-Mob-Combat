package me.Thelnfamous1.bettermobcombat.mixin;

import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import net.bettercombat.api.AttackHand;
import net.bettercombat.logic.PlayerAttackProperties;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin({Enchantment.class})
public class EnchantmentMixin {
    public EnchantmentMixin() {
    }

    @Inject(
        method = {"getSlotItems"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void getEquipmentFix(LivingEntity entity, CallbackInfoReturnable<Map<EquipmentSlot, ItemStack>> cir) {
        if (entity instanceof Mob mob) {
            int comboCount = ((PlayerAttackProperties)mob).getComboCount();
            AttackHand currentHand = MobAttackHelper.getCurrentAttack(mob, comboCount);
            if (currentHand != null && currentHand.isOffHand()) {
                Map<EquipmentSlot, ItemStack> map = cir.getReturnValue();
                if (map.get(EquipmentSlot.MAINHAND) != null) {
                    map.remove(EquipmentSlot.MAINHAND);
                }

                ItemStack offHandStack = mob.getOffhandItem();
                if (!offHandStack.isEmpty()) {
                    map.put(EquipmentSlot.OFFHAND, offHandStack);
                }

                cir.setReturnValue(map);
            }
        }

    }
}