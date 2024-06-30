package me.Thelnfamous1.bettermobcombat.mixin.client;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.world.entity.Mob;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(HumanoidMobRenderer.class)
public abstract class HumanoidMobRendererMixin<T extends Mob, M extends HumanoidModel<T>> extends MobRendererMixin<T, M>{

    protected HumanoidMobRendererMixin(EntityRendererProvider.Context $$0) {
        super($$0);
    }

    // This causes problems, disabling for now
        /*
    @Override
    protected void bettermobcombat$handleFirstPersonRender(T mob) {
        if (FirstPersonMode.isFirstPersonPass()) {
            var animationApplier = MobPlayerModel.bettermobcombat$getAnimation(mob);
            var config = animationApplier.getFirstPersonConfiguration();

            if (mob == Minecraft.getInstance().getCameraEntity()) {
                // Hiding all parts, because they should not be visible in first person
                setAllPartsVisible(false);
                // Showing arms based on configuration
                var showRightArm = config.isShowRightArm();
                var showLeftArm = config.isShowLeftArm();
                this.model.rightArm.visible = showRightArm;
                this.model.leftArm.visible = showLeftArm;
            }
        }
    }

    @Unique
    protected void setAllPartsVisible(boolean visible) {
        this.model.head.visible = visible;
        this.model.body.visible = visible;
        this.model.leftLeg.visible = visible;
        this.model.rightLeg.visible = visible;
        this.model.rightArm.visible = visible;
        this.model.leftArm.visible = visible;

        this.model.hat.visible = visible;
    }
     */
}
