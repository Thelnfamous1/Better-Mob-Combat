package me.Thelnfamous1.bettermobcombat.minecraftApi;

import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Mob;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

/**
 * Animation factory, the factory will be invoked whenever a mob is constructed on the client.
 * The returned animation will be automatically registered and added to playerAssociated data.
 * <p>
 * {@link MobAnimationAccess#REGISTER_ANIMATION_EVENT} is invoked <strong>after</strong> factories are done.
 */
public interface MobAnimationFactory {

    FactoryHolder ANIMATION_DATA_FACTORY = new FactoryHolder();

    @Nullable IAnimation invoke(@NotNull Mob mob);

    class FactoryHolder {
        private FactoryHolder() {}

        private static final List<Function<Mob, DataHolder>> factories = new ArrayList<>();

        /**
         * Animation factory
         * @param id       animation id or <code>null</code> if you don't want to add to playerAssociated data
         * @param priority animation priority
         * @param factory  animation factory
         */
        public void registerFactory(@Nullable ResourceLocation id, int priority, @NotNull MobAnimationFactory factory) {
            factories.add(player -> Optional.ofNullable(factory.invoke(player)).map(animation -> new DataHolder(id, priority, animation)).orElse(null));
        }

        @ApiStatus.Internal
        private record DataHolder(@Nullable ResourceLocation id, int priority, @NotNull IAnimation animation) {}

        @ApiStatus.Internal
        public void prepareAnimations(Mob mob, AnimationStack playerStack, Map<ResourceLocation, IAnimation> animationMap) {
            for (Function<Mob, DataHolder> factory: factories) {
                DataHolder dataHolder = factory.apply(mob);
                if (dataHolder != null) {
                    playerStack.addAnimLayer(dataHolder.priority(), dataHolder.animation());
                    if (dataHolder.id() != null) {
                        animationMap.put(dataHolder.id(), dataHolder.animation());
                    }
                }
            }
        }
    }

}