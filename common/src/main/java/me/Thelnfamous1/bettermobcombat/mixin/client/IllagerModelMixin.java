package me.Thelnfamous1.bettermobcombat.mixin.client;

import me.Thelnfamous1.bettermobcombat.api.MobAttackAnimation;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.IllagerModel;
import net.minecraft.world.entity.monster.AbstractIllager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(IllagerModel.class)
public abstract class IllagerModelMixin<T extends AbstractIllager> extends HierarchicalModel<T> {

    @ModifyVariable(method = "setupAnim(Lnet/minecraft/world/entity/monster/AbstractIllager;FFFFF)V", at = @At(value = "STORE", ordinal = 0), ordinal = 0)
    private boolean modifyUseCrossedArms(boolean useCrossedArms, T illager, float $$1, float $$2, float $$3, float $$4, float $$5){
        if(((MobAttackAnimation)illager).bettermobcombat$hasActiveAttackAnimation()){
            return false;
        }
        return useCrossedArms;
    }
}
