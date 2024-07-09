package me.Thelnfamous1.bettermobcombat.mixin;

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
import me.Thelnfamous1.bettermobcombat.BetterMobCombatClient;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.animation.*;
import net.bettercombat.client.animation.modifier.HarshAdjustmentModifier;
import net.bettercombat.client.animation.modifier.TransmissionSpeedModifier;
import net.bettercombat.compatibility.CompatibilityFlags;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Optional;

@Mixin(value = Mob.class)
public abstract class MobMixin_AttackAnimation extends LivingEntity implements PlayerAttackAnimatable, IAnimatedPlayer {
    @Shadow public abstract boolean isLeftHanded();

    @Unique
    private final AttackAnimationSubStack bettermobcombat$attackAnimation = new AttackAnimationSubStack(this.bettermobcombat$createAttackAdjustment());
    @Unique
    private final PoseSubStack bettermobcombat$mainHandBodyPose = new PoseSubStack(this.bettermobcombat$createPoseAdjustment(), true, true);
    @Unique
    private final PoseSubStack bettermobcombat$mainHandItemPose = new PoseSubStack(null, false, true);
    @Unique
    private final PoseSubStack bettermobcombat$offHandBodyPose = new PoseSubStack(null, true, false);
    @Unique
    private final PoseSubStack bettermobcombat$offHandItemPose = new PoseSubStack(null, false, true);
    protected MobMixin_AttackAnimation(EntityType<? extends Mob> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Unique
    private AdjustmentModifier bettermobcombat$createAttackAdjustment() {
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
    private AdjustmentModifier bettermobcombat$createPoseAdjustment() {
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
                        if (!this.bettermobcombat$mainHandItemPose.lastAnimationUsesBodyChannel && this.isShiftKeyDown()) {
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

    @Inject(
            method = {"<init>"},
            at = {@At("TAIL")}
    )
    private void post_init(EntityType<?> $$0, Level $$1, CallbackInfo ci) {
        AnimationStack stack = this.getAnimationStack();
        stack.addAnimLayer(1, this.bettermobcombat$offHandItemPose.base);
        stack.addAnimLayer(2, this.bettermobcombat$offHandBodyPose.base);
        stack.addAnimLayer(3, this.bettermobcombat$mainHandItemPose.base);
        stack.addAnimLayer(4, this.bettermobcombat$mainHandBodyPose.base);
        stack.addAnimLayer(2000, this.bettermobcombat$attackAnimation.base);
        this.bettermobcombat$mainHandBodyPose.configure = this::bettermobcombat$updateAnimationByCurrentActivity;
        this.bettermobcombat$offHandBodyPose.configure = this::bettermobcombat$updateAnimationByCurrentActivity;
    }

    @Override
    public void updateAnimationsOnTick() {
        if(!this.level().isClientSide){
            return;
        }
        boolean isLeftHanded = this.isLeftHanded();
        boolean hasActiveAttackAnimation = this.bettermobcombat$attackAnimation.base.getAnimation() != null && this.bettermobcombat$attackAnimation.base.getAnimation().isActive();
        ItemStack mainHandStack = this.getMainHandItem();
        if (!this.swinging && !this.isSwimming() && !this.isUsingItem() && !Services.PLATFORM.isCastingSpell(this) && !CrossbowItem.isCharged(mainHandStack)) {
            if (hasActiveAttackAnimation) {
                // Mobs override LivingEntity#tickHeadTurn to tick their body controller instead
                //((LivingEntityAccessor) mob).invokeTurnHead(mob.getYHeadRot(), 0.0F);
                super.tickHeadTurn(this.getYHeadRot(), 0.0F);
            }

            KeyframeAnimation newMainHandPose = null;
            WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(mainHandStack);
            if (mainHandAttributes != null && mainHandAttributes.pose() != null) {
                newMainHandPose = AnimationRegistry.animations.get(mainHandAttributes.pose());
            }

            KeyframeAnimation newOffHandPose = null;
            if (MobAttackHelper.isDualWielding((Mob) (Object) this)) {
                WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(this.getOffhandItem());
                if (offHandAttributes != null && offHandAttributes.offHandPose() != null) {
                    newOffHandPose = AnimationRegistry.animations.get(offHandAttributes.offHandPose());
                }
            }

            this.bettermobcombat$mainHandItemPose.setPose(newMainHandPose, isLeftHanded);
            this.bettermobcombat$offHandItemPose.setPose(newOffHandPose, isLeftHanded);
            if (!MobAttackHelper.isTwoHandedWielding((Mob) (Object) this) && (this.bettermobcombat$isWalking() || this.isShiftKeyDown())) {
                newMainHandPose = null;
                newOffHandPose = null;
            }

            this.bettermobcombat$mainHandBodyPose.setPose(newMainHandPose, isLeftHanded);
            this.bettermobcombat$offHandBodyPose.setPose(newOffHandPose, isLeftHanded);
        } else {
            this.bettermobcombat$mainHandBodyPose.setPose(null, isLeftHanded);
            this.bettermobcombat$mainHandItemPose.setPose(null, isLeftHanded);
            this.bettermobcombat$offHandBodyPose.setPose(null, isLeftHanded);
            this.bettermobcombat$offHandItemPose.setPose(null, isLeftHanded);
        }
    }

    @Unique
    private boolean bettermobcombat$isWalking() {
        return !this.isDeadOrDying() && (this.isSwimming() || this.getDeltaMovement().horizontalDistance() > 0.03);
    }

    @Override
    public void playAttackAnimation(String name, AnimatedHand animatedHand, float length, float upswing) {
        if(!this.level().isClientSide){
            return;
        }
        try {
            KeyframeAnimation animation = AnimationRegistry.animations.get(name);
            KeyframeAnimation.AnimationBuilder copy = animation.mutableCopy();
            this.bettermobcombat$updateAnimationByCurrentActivity(copy);
            copy.torso.fullyEnablePart(true);
            copy.head.pitch.setEnabled(false);
            float speed = (float) animation.endTick / length;
            boolean mirror = animatedHand.isOffHand();
            if (this.isLeftHanded()) {
                mirror = !mirror;
            }

            int fadeIn = copy.beginTick;
            float upswingSpeed = speed / BetterCombat.config.getUpswingMultiplier();
            float downwindSpeed = (float) ((double) speed * Mth.lerp(Math.max((double) BetterCombat.config.getUpswingMultiplier() - 0.5, 0.0) / 0.5, 1.0F - upswing, upswing / (1.0F - upswing)));
            this.bettermobcombat$attackAnimation.speed.set(upswingSpeed, List.of(new TransmissionSpeedModifier.Gear(length * upswing, downwindSpeed), new TransmissionSpeedModifier.Gear(length, speed)));
            this.bettermobcombat$attackAnimation.mirror.setEnabled(mirror);
            CustomAnimationPlayer player = new CustomAnimationPlayer(copy.build(), 0);
            player.setFirstPersonMode(CompatibilityFlags.firstPersonRender() ? FirstPersonMode.THIRD_PERSON_MODEL : FirstPersonMode.NONE);
            player.setFirstPersonConfiguration(this.bettermobcombat$firstPersonConfig(animatedHand));
            this.bettermobcombat$attackAnimation.base.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeIn, Ease.INOUTSINE), player);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Unique
    private FirstPersonConfiguration bettermobcombat$firstPersonConfig(AnimatedHand animatedHand) {
        boolean showRightItem = true;
        boolean showLeftItem = BetterMobCombatClient.getBetterCombatClientConfig().isShowingOtherHandFirstPerson || animatedHand == AnimatedHand.TWO_HANDED;
        boolean showRightArm = showRightItem && BetterMobCombatClient.getBetterCombatClientConfig().isShowingArmsInFirstPerson;
        boolean showLeftArm = showLeftItem && BetterMobCombatClient.getBetterCombatClientConfig().isShowingArmsInFirstPerson;
        FirstPersonConfiguration config = new FirstPersonConfiguration(showRightArm, showLeftArm, showRightItem, showLeftItem);
        return config;
    }

    @Unique
    private void bettermobcombat$updateAnimationByCurrentActivity(KeyframeAnimation.AnimationBuilder animation) {
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
                if (this.bettermobcombat$isMounting()) {
                    StateCollectionHelper.configure(animation.rightLeg, false, false);
                    StateCollectionHelper.configure(animation.leftLeg, false, false);
                }

        }
    }

    @Unique
    private boolean bettermobcombat$isMounting() {
        return this.getVehicle() != null;
    }

    @Override
    public void stopAttackAnimation(float length) {
        if(!this.level().isClientSide){
            return;
        }
        IAnimation currentAnimation = this.bettermobcombat$attackAnimation.base.getAnimation();
        if (currentAnimation != null && currentAnimation instanceof KeyframeAnimationPlayer) {
            int fadeOut = Math.round(length);
            this.bettermobcombat$attackAnimation.adjustmentModifier.fadeOut(fadeOut);
            this.bettermobcombat$attackAnimation.base.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(fadeOut, Ease.INOUTSINE), null);
        }

    }
}
