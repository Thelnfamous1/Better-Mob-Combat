package me.Thelnfamous1.bettermobcombat.mixin;

import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Mob.class)
public interface MobAccessor {

    @Accessor("handItems")
    NonNullList<ItemStack> bettermobcombat$getHandItems();
}
