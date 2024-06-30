package me.Thelnfamous1.bettermobcombat.api;

import dev.kosmx.playerAnim.api.IPlayer;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.core.impl.event.Event;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MobAnimationAccess {

    /**
     * Get the animation stack for a mob entity on the client.
     * <p>
     * Or you can use {@code ((IPlayer) mob).getAnimationStack();}
     *
     * @param mob The Mob object
     * @return The mob's animation stack
     */
    public static AnimationStack getPlayerAnimLayer(Mob mob) throws IllegalArgumentException {
        if (mob instanceof IPlayer) {
            return ((IPlayer) mob).getAnimationStack();
        } else throw new IllegalArgumentException(mob + " is not a mob or library mixins failed");
    }

    /**
     * Allows mods to store animation layers associated with mob.
     * Stored data does not get automatically registered.
     * @param mob mob entity
     * @return  data accessor type, you can use get() and set() on it (kotlin getter/setter compatible)
     * @throws IllegalArgumentException if the given argument is not a mob, or api mixins have failed (normally never)
     * @implNote data is stored in the mob object (using mixins), using it is more efficient than any objectMap as objectMap solution does not know when to delete the data.
     */
    public static MobAssociatedAnimationData getPlayerAssociatedData(@NotNull Mob mob) {
        if (mob instanceof IAnimatedPlayer animatedPlayer) {
            return new MobAssociatedAnimationData(animatedPlayer);
        } else throw new IllegalArgumentException(mob + " is not a mob or library mixins failed");
    }

    /**
     * If you don't want to create your own mixin, you can use this event to add animation to mobs<br>
     * <b>The event will fire for every mob</b> and if the mob reloads, it will fire again.<br>
     * <hr>
     * NOTE: When the event fires, {@link IPlayer#getAnimationStack()} will be <code>null</code>, you'll have to use the given stack.
     */
    public static final Event<AnimationRegister> REGISTER_ANIMATION_EVENT = new Event<>(AnimationRegister.class, listeners -> (mob, animationStack) -> {
        for (AnimationRegister listener : listeners) {
            listener.registerAnimation(mob, animationStack);
        }
    });

    @FunctionalInterface
    public interface AnimationRegister {
        /**
         * Player object is in construction, it will be invoked when you can register animation
         * It will be invoked for every mob only ONCE (it isn't a tick function)
         * @param mob         Client mob object, can be the main mob or other mob
         * @param animationStack the mobs AnimationStack, unique for every mob
         */
        void registerAnimation(@NotNull Mob mob, @NotNull AnimationStack animationStack);
    }

    public static class MobAssociatedAnimationData {
        @NotNull
        private final IAnimatedPlayer mob;

        public MobAssociatedAnimationData(@NotNull IAnimatedPlayer mob) {
            this.mob = mob;
        }

        /**
         * Get an animation associated with the mob
         * @param id    Animation identifier, please start with your modid to avoid collision
         * @return      animation or <code>null</code> if not exists
         * @apiNote     This function does <strong>not</strong> register the animation, just store it.
         */
        @Nullable public IAnimation get(@NotNull ResourceLocation id) {
            return mob.playerAnimator_getAnimation(id);
        }

        /**
         * Set an animation associated with the mob
         *
         * @param id        Animation identifier. Please don't override/remove other mod animations, always use your modid!
         * @param animation animation to store in the mob, <code>null</code> to clear stored animation
         * @return          The previously stored animation.
         */
        @Nullable public IAnimation set(@NotNull ResourceLocation id, @Nullable IAnimation animation) {
            return mob.playerAnimator_setAnimation(id, animation);
        }
    }
}