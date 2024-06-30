package me.Thelnfamous1.bettermobcombat.mixin.client;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import me.Thelnfamous1.bettermobcombat.client.MobModelHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.ZombieVillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.monster.Zombie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ZombieVillagerModel.class)
public abstract class ZombieVillagerModelMixin<T extends Zombie> extends HumanoidModel<T> {

    public ZombieVillagerModelMixin(ModelPart $$0) {
        super($$0);
    }

    @WrapWithCondition(method = "setupAnim",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/AnimationUtils;animateZombieArms(Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;ZFF)V")
    )
    private boolean onlyAnimateZombieArmsIfAllowed(ModelPart leftArm, ModelPart rightArm, boolean aggressive, float attackTime, float bob,
                                                   T zombie,
                                                   float $$1,
                                                   float $$2,
                                                   float $$3,
                                                   float $$4,
                                                   float $$5) {
        return !MobModelHelper.isAnimating(zombie);
    }
}
