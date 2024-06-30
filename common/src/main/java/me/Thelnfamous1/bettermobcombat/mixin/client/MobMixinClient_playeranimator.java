package me.Thelnfamous1.bettermobcombat.mixin.client;

import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import me.Thelnfamous1.bettermobcombat.api.MobAnimationAccess;
import me.Thelnfamous1.bettermobcombat.api.MobAnimationFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;

@Mixin(Mob.class)
public abstract class MobMixinClient_playeranimator extends LivingEntity implements IAnimatedPlayer {
    @Unique
    private final Map<ResourceLocation, IAnimation> bettermobcombat$modAnimationData = new HashMap<>();
    @Unique
    private final AnimationStack bettermobcombat$animationStack = bettermobcombat$createAnimationStack();
    @Unique
    private final AnimationApplier bettermobcombat$animationApplier = new AnimationApplier(bettermobcombat$animationStack);
    protected MobMixinClient_playeranimator(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    @SuppressWarnings("ConstantConditions")
    @Unique
    private AnimationStack bettermobcombat$createAnimationStack() {
        AnimationStack stack = new AnimationStack();
        if (this.level().isClientSide) {
            MobAnimationFactory.ANIMATION_DATA_FACTORY.prepareAnimations((Mob) (Object) this, stack, bettermobcombat$modAnimationData);
            MobAnimationAccess.REGISTER_ANIMATION_EVENT.invoker().registerAnimation((Mob)(Object) this, stack);
        }
        return stack;
    }

    @Override
    public AnimationStack getAnimationStack() {
        return bettermobcombat$animationStack;
    }

    @Override
    public AnimationApplier playerAnimator_getAnimation() {
        return bettermobcombat$animationApplier;
    }

    @Override
    public @Nullable IAnimation playerAnimator_getAnimation(@NotNull ResourceLocation id) {
        return bettermobcombat$modAnimationData.get(id);
    }

    @Override
    public @Nullable IAnimation playerAnimator_setAnimation(@NotNull ResourceLocation id, @Nullable IAnimation animation) {
        if (animation == null) {
            return bettermobcombat$modAnimationData.remove(id);
        } else {
            return bettermobcombat$modAnimationData.put(id, animation);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (this.level().isClientSide) {
            bettermobcombat$animationStack.tick();
        }
    }
}
