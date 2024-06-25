package me.Thelnfamous1.bettermobcombat.mixin.client;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.api.layered.modifier.AdjustmentModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.AnimationApplier;
import me.Thelnfamous1.bettermobcombat.minecraftApi.MobAnimationAccess;
import me.Thelnfamous1.bettermobcombat.minecraftApi.MobAnimationFactory;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.BetterCombatClient;
import net.bettercombat.client.animation.*;
import net.bettercombat.client.animation.modifier.HarshAdjustmentModifier;
import net.bettercombat.client.animation.modifier.TransmissionSpeedModifier;
import net.bettercombat.compatibility.CompatibilityFlags;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.mixin.LivingEntityAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mixin(Mob.class)
public abstract class MobMixinClient extends LivingEntity implements PlayerAttackAnimatable, IAnimatedPlayer {
    @Unique
    private final AttackAnimationSubStack attackAnimation = new AttackAnimationSubStack(this.createAttackAdjustment());
    @Unique
    private final PoseSubStack mainHandBodyPose = new PoseSubStack(this.createPoseAdjustment(), true, true);
    @Unique
    private final PoseSubStack mainHandItemPose = new PoseSubStack(null, false, true);
    @Unique
    private final PoseSubStack offHandBodyPose = new PoseSubStack(null, true, false);
    @Unique
    private final PoseSubStack offHandItemPose = new PoseSubStack(null, false, true);
    @Unique
    private final Map<ResourceLocation, IAnimation> modAnimationData = new HashMap<>();
    @Unique
    private final AnimationStack animationStack = createAnimationStack();
    @Unique
    private final AnimationApplier animationApplier = new AnimationApplier(animationStack);
    protected MobMixinClient(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    @SuppressWarnings("ConstantConditions")
    @Unique
    private AnimationStack createAnimationStack() {
        AnimationStack stack = new AnimationStack();
        if (this.level().isClientSide) {
            MobAnimationFactory.ANIMATION_DATA_FACTORY.prepareAnimations((Mob) (Object) this, stack, modAnimationData);
            MobAnimationAccess.REGISTER_ANIMATION_EVENT.invoker().registerAnimation((Mob)(Object) this, stack);
        }
        return stack;
    }

    @Override
    public AnimationStack getAnimationStack() {
        return animationStack;
    }

    @Override
    public AnimationApplier playerAnimator_getAnimation() {
        return animationApplier;
    }

    @Override
    public @Nullable IAnimation playerAnimator_getAnimation(@NotNull ResourceLocation id) {
        return modAnimationData.get(id);
    }

    @Override
    public @Nullable IAnimation playerAnimator_setAnimation(@NotNull ResourceLocation id, @Nullable IAnimation animation) {
        if (animation == null) {
            return modAnimationData.remove(id);
        } else {
            return modAnimationData.put(id, animation);
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tick(CallbackInfo ci) {
        if (this.level().isClientSide) {
            animationStack.tick();
        }
    }

    /*
    @Inject(
            method = {"aiStep"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/input/Input;tick(ZF)V",
                    shift = At.Shift.AFTER
            )}
    )
    private void tickMovement_ModifyInput(CallbackInfo ci) {
        ServerConfig config = BetterCombat.config;
        double multiplier = Math.min(Math.max((double)config.movement_speed_while_attacking, 0.0), 1.0);
        if (multiplier != 1.0) {
            LocalPlayer clientPlayer = (LocalPlayer)this;
            if (!clientPlayer.isPassenger() || config.movement_speed_effected_while_mounting) {
                MinecraftClient_BetterCombat client = (MinecraftClient_BetterCombat) Minecraft.getInstance();
                float swingProgress = client.getSwingProgress();
                if ((double)swingProgress < 0.98) {
                    if (config.movement_speed_applied_smoothly) {
                        double p2 = 0.0;
                        if ((double)swingProgress <= 0.5) {
                            p2 = MathHelper.easeOutCubic((double)(swingProgress * 2.0F));
                        } else {
                            p2 = MathHelper.easeOutCubic(1.0 - ((double)swingProgress - 0.5) * 2.0);
                        }

                        multiplier = (double)((float)(1.0 - (1.0 - multiplier) * p2));
                    }

                    Input var10000 = clientPlayer.input;
                    var10000.forwardImpulse = (float)((double)var10000.forwardImpulse * multiplier);
                    var10000 = clientPlayer.input;
                    var10000.leftImpulse = (float)((double)var10000.leftImpulse * multiplier);
                }

            }
        }
    }
     */

    @Inject(
            method = {"<init>"},
            at = {@At("TAIL")}
    )
    private void postInit(EntityType<?> $$0, Level $$1, CallbackInfo ci) {
        AnimationStack stack = this.getAnimationStack();
        stack.addAnimLayer(1, this.offHandItemPose.base);
        stack.addAnimLayer(2, this.offHandBodyPose.base);
        stack.addAnimLayer(3, this.mainHandItemPose.base);
        stack.addAnimLayer(4, this.mainHandBodyPose.base);
        stack.addAnimLayer(2000, this.attackAnimation.base);
        this.mainHandBodyPose.configure = this::updateAnimationByCurrentActivity;
        this.offHandBodyPose.configure = this::updateAnimationByCurrentActivity;
    }

    @Override
    public void updateAnimationsOnTick() {
        Mob mob = (Mob) (Object) this;
        boolean isLeftHanded = this.isMainArmLeft();
        boolean hasActiveAttackAnimation = this.attackAnimation.base.getAnimation() != null && this.attackAnimation.base.getAnimation().isActive();
        ItemStack mainHandStack = mob.getMainHandItem();
        if (!mob.swinging && !mob.isSwimming() && !mob.isUsingItem() && !Services.PLATFORM.isCastingSpell(mob) && !CrossbowItem.isCharged(mainHandStack)) {
            if (hasActiveAttackAnimation) {
                ((LivingEntityAccessor) mob).invokeTurnHead(mob.getYHeadRot(), 0.0F);
            }

            KeyframeAnimation newMainHandPose = null;
            WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(mainHandStack);
            if (mainHandAttributes != null && mainHandAttributes.pose() != null) {
                newMainHandPose = AnimationRegistry.animations.get(mainHandAttributes.pose());
            }

            KeyframeAnimation newOffHandPose = null;
            if (MobAttackHelper.isDualWielding(mob)) {
                WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(mob.getOffhandItem());
                if (offHandAttributes != null && offHandAttributes.offHandPose() != null) {
                    newOffHandPose = AnimationRegistry.animations.get(offHandAttributes.offHandPose());
                }
            }

            this.mainHandItemPose.setPose(newMainHandPose, isLeftHanded);
            this.offHandItemPose.setPose(newOffHandPose, isLeftHanded);
            if (!MobAttackHelper.isTwoHandedWielding(mob) && (this.isWalking() || this.isShiftKeyDown())) {
                newMainHandPose = null;
                newOffHandPose = null;
            }

            this.mainHandBodyPose.setPose(newMainHandPose, isLeftHanded);
            this.offHandBodyPose.setPose(newOffHandPose, isLeftHanded);
        } else {
            this.mainHandBodyPose.setPose(null, isLeftHanded);
            this.mainHandItemPose.setPose(null, isLeftHanded);
            this.offHandBodyPose.setPose(null, isLeftHanded);
            this.offHandItemPose.setPose(null, isLeftHanded);
        }
    }

    @Override
    public void playAttackAnimation(String name, AnimatedHand animatedHand, float length, float upswing) {
        try {
            KeyframeAnimation animation = AnimationRegistry.animations.get(name);
            KeyframeAnimation.AnimationBuilder copy = animation.mutableCopy();
            this.updateAnimationByCurrentActivity(copy);
            copy.torso.fullyEnablePart(true);
            copy.head.pitch.setEnabled(false);
            float speed = (float) animation.endTick / length;
            boolean mirror = animatedHand.isOffHand();
            if (this.isMainArmLeft()) {
                mirror = !mirror;
            }

            int fadeIn = copy.beginTick;
            float upswingSpeed = speed / BetterCombat.config.getUpswingMultiplier();
            float downwindSpeed = (float) ((double) speed * Mth.lerp(Math.max((double) BetterCombat.config.getUpswingMultiplier() - 0.5, 0.0) / 0.5, 1.0F - upswing, upswing / (1.0F - upswing)));
            this.attackAnimation.speed.set(upswingSpeed, List.of(new TransmissionSpeedModifier.Gear(length * upswing, downwindSpeed), new TransmissionSpeedModifier.Gear(length, speed)));
            this.attackAnimation.mirror.setEnabled(mirror);
            CustomAnimationPlayer player = new CustomAnimationPlayer(copy.build(), 0);
            player.setFirstPersonMode(CompatibilityFlags.firstPersonRender() ? FirstPersonMode.THIRD_PERSON_MODEL : FirstPersonMode.NONE);
            player.setFirstPersonConfiguration(this.firstPersonConfig(animatedHand));
            this.attackAnimation.base.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeIn, Ease.INOUTSINE), player);
        } catch (Exception var13) {
            var13.printStackTrace();
        }

    }

    @Unique
    private AdjustmentModifier createAttackAdjustment() {
        return new AdjustmentModifier((partName) -> {
            float rotationX = 0.0F;
            float rotationY = 0.0F;
            float rotationZ = 0.0F;
            float offsetX = 0.0F;
            float offsetY = 0.0F;
            float offsetZ = 0.0F;
            float pitch;
            if (FirstPersonMode.isFirstPersonPass()) {
                pitch = this.getXRot();
                pitch = (float) Math.toRadians(pitch);
                switch (partName) {
                    case "body":
                        rotationX -= pitch;
                        if (pitch < 0.0F) {
                            double offset = Math.abs(Math.sin(pitch));
                            offsetY = (float) ((double) offsetY + offset * 0.5);
                            offsetZ = (float) ((double) offsetZ - offset);
                        }
                        break;
                    default:
                        return Optional.empty();
                }
            } else {
                pitch = this.getXRot();
                pitch = (float) Math.toRadians(pitch);
                switch (partName) {
                    case "body":
                        rotationX -= pitch * 0.75F;
                        break;
                    case "rightArm":
                    case "leftArm":
                        rotationX += pitch * 0.25F;
                        break;
                    case "rightLeg":
                    case "leftLeg":
                        rotationX = (float) ((double) rotationX - (double) pitch * 0.75);
                        break;
                    default:
                        return Optional.empty();
                }
            }

            return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(rotationX, rotationY, rotationZ), new Vec3f(offsetX, offsetY, offsetZ)));
        });
    }

    @Unique
    private AdjustmentModifier createPoseAdjustment() {
        return new HarshAdjustmentModifier((partName) -> {
            float rotationX = 0.0F;
            float rotationY = 0.0F;
            float rotationZ = 0.0F;
            float offsetX = 0.0F;
            float offsetY = 0.0F;
            float offsetZ = 0.0F;
            if (!FirstPersonMode.isFirstPersonPass()) {
                switch (partName) {
                    case "rightArm":
                    case "leftArm":
                        if (!this.mainHandItemPose.lastAnimationUsesBodyChannel && this.isShiftKeyDown()) {
                            offsetY += 3.0F;
                        }
                        break;
                    default:
                        return Optional.empty();
                }
            }

            return Optional.of(new AdjustmentModifier.PartModifier(new Vec3f(rotationX, rotationY, rotationZ), new Vec3f(offsetX, offsetY, offsetZ)));
        });
    }

    @Unique
    private void updateAnimationByCurrentActivity(KeyframeAnimation.AnimationBuilder animation) {
        Pose pose = this.getPose();
        switch (pose) {
            case SWIMMING:
                StateCollectionHelper.configure(animation.rightLeg, false, false);
                StateCollectionHelper.configure(animation.leftLeg, false, false);
            case STANDING:
            case FALL_FLYING:
            case SLEEPING:
            case SPIN_ATTACK:
            case CROUCHING:
            case LONG_JUMPING:
            case DYING:
            default:
                if (this.isMounting()) {
                    StateCollectionHelper.configure(animation.rightLeg, false, false);
                    StateCollectionHelper.configure(animation.leftLeg, false, false);
                }

        }
    }

    @Unique
    private boolean isWalking() {
        return !this.isDeadOrDying() && (this.isSwimming() || this.getDeltaMovement().horizontalDistance() > 0.03);
    }

    @Unique
    private boolean isMounting() {
        return this.getVehicle() != null;
    }

    @Unique
    public boolean isMainArmLeft() {
        return this.getMainArm() == HumanoidArm.LEFT;
    }

    @Override
    public void stopAttackAnimation(float length) {
        IAnimation currentAnimation = this.attackAnimation.base.getAnimation();
        if (currentAnimation != null && currentAnimation instanceof KeyframeAnimationPlayer) {
            int fadeOut = Math.round(length);
            this.attackAnimation.adjustmentModifier.fadeOut(fadeOut);
            this.attackAnimation.base.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeOut, Ease.INOUTSINE), null);
        }

    }

    @Unique
    private FirstPersonConfiguration firstPersonConfig(AnimatedHand animatedHand) {
        boolean showRightItem = true;
        boolean showLeftItem = BetterCombatClient.config.isShowingOtherHandFirstPerson || animatedHand == AnimatedHand.TWO_HANDED;
        boolean showRightArm = showRightItem && BetterCombatClient.config.isShowingArmsInFirstPerson;
        boolean showLeftArm = showLeftItem && BetterCombatClient.config.isShowingArmsInFirstPerson;
        FirstPersonConfiguration config = new FirstPersonConfiguration(showRightArm, showLeftArm, showRightItem, showLeftItem);
        return config;
    }
}
