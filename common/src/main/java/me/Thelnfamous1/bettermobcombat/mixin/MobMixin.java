package me.Thelnfamous1.bettermobcombat.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.Thelnfamous1.bettermobcombat.BetterMobCombatCommon;
import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.api.client.BetterMobCombatClientEvents;
import me.Thelnfamous1.bettermobcombat.client.collision.MobTargetFinder;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackStrength;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.animation.PlayerAttackAnimatable;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.logic.PlayerAttackProperties;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements EntityPlayer_BetterCombat, MobAttackStrength, MobAttackWindup, PlayerAttackProperties {

    @Unique
    private int comboCount = 0;
    @Unique
    private Multimap<Attribute, AttributeModifier> dualWieldingAttributeMap;
    @Unique
    private static final UUID dualWieldingSpeedModifierId = UUID.fromString("6b364332-0dc4-11ed-861d-0242ac120002");
    @Unique
    private AttackHand lastAttack;
    @Unique
    private ItemStack lastItemInMainHand = ItemStack.EMPTY;
    @Unique
    private int attackCooldown;

    @Shadow
    @Nullable
    public abstract LivingEntity getTarget();

    @Shadow
    public abstract boolean doHurtTarget(Entity $$0);

    @Unique
    private ItemStack upswingStack;
    @Unique
    private ItemStack lastAttackedWithItemStack;
    @Unique
    private int upswingTicks = 0;
    @Unique
    private int lastAttacked = 1000;
    @Unique
    private float lastSwingDuration = 0.0F;
    @Unique
    private int comboReset = 0;
    @Unique
    private List<Entity> targetsInReach = null;

    protected MobMixin(EntityType<? extends LivingEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public @Nullable AttackHand getCurrentAttack() {
        if (this.comboCount < 0) {
            return null;
        } else {
            Mob player = (Mob) (Object) this;
            return MobAttackHelper.getCurrentAttack(player, this.comboCount);
        }
    }

    @Override
    public float bettermobcombat$getCurrentItemAttackStrengthDelay() {
        double attackSpeed;
        if (this.getAttribute(Attributes.ATTACK_SPEED) != null) {
            attackSpeed = this.getAttributeValue(Attributes.ATTACK_SPEED);
        } else{
            // Mimicking logic used in LivingEntityMixin#getAttributeValue_Inject
            ItemStack activeWeapon = !this.level().isClientSide && this.getComboCount() > 0 && MobAttackHelper.shouldAttackWithOffHand(this, this.getComboCount()) ?
                    this.getOffhandItem():
                    this.getMainHandItem();
            Collection<AttributeModifier> speedMods = activeWeapon.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_SPEED);
            attackSpeed = MobCombatHelper.calculateAttributeValue(Attributes.ATTACK_SPEED, Attributes.ATTACK_SPEED.getDefaultValue(), speedMods);
        }
        return (float) (1.0D / attackSpeed * 20.0D);
    }

    @Override
    public double bettercombat$getAttackStrengthScale(float partialTick) {
        return Mth.clamp(((float) this.attackStrengthTicker + partialTick) / this.bettermobcombat$getCurrentItemAttackStrengthDelay(), 0.0F, 1.0F);
    }

    @Override
    public void bettercombat$resetAttackStrengthTicker() {
        this.attackStrengthTicker = 0;
    }

    @Override
    public void swing(InteractionHand $$0) {
        super.swing($$0);
        this.bettercombat$resetAttackStrengthTicker();
    }

    @Inject(
            method = {"tick"},
            at = {@At("TAIL")}
    )
    public void post_Tick(CallbackInfo ci) {
        if (this.level().isClientSide) {
            ((PlayerAttackAnimatable) this).updateAnimationsOnTick();
        }

        this.updateDualWieldingSpeedBoost();

        // Copy of attack strength logic in Player#tick
        ++this.attackStrengthTicker;
        ItemStack itemstack = this.getMainHandItem();
        if (!ItemStack.matches(this.lastItemInMainHand, itemstack)) {
            if (!ItemStack.isSameItem(this.lastItemInMainHand, itemstack)) {
                this.bettercombat$resetAttackStrengthTicker();
            }

            this.lastItemInMainHand = itemstack.copy();
        }
    }

    /*
    @ModifyVariable(
            method = {"attack"},
            at = @At("STORE"),
            ordinal = 3
    )
    private boolean disableSweeping(boolean value) {
        if (BetterCombat.config.allow_vanilla_sweeping) {
            return value;
        } else {
            Player player = (Player)this;
            AttackHand currentHand = PlayerAttackHelper.getCurrentAttack(player, this.comboCount);
            return currentHand != null ? false : value;
        }
    }
     */

    @Inject(
            method = {"getItemBySlot"},
            at = {@At("HEAD")},
            cancellable = true
    )
    public void getEquippedStack_Pre(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        boolean mainHandHasTwoHanded = false;
        ItemStack mainHandStack = MobCombatHelper.getDirectMainhand((Mob) (Object) this);
        WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(mainHandStack);
        if (mainHandAttributes != null && mainHandAttributes.isTwoHanded()) {
            mainHandHasTwoHanded = true;
        }

        boolean offHandHasTwoHanded = false;
        ItemStack offHandStack = MobCombatHelper.getDirectOffhand((Mob) (Object) this);
        WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(offHandStack);
        if (offHandAttributes != null && offHandAttributes.isTwoHanded()) {
            offHandHasTwoHanded = true;
        }

        if (slot == EquipmentSlot.OFFHAND && (mainHandHasTwoHanded || offHandHasTwoHanded)) {
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
        }
    }

    @Unique
    private void updateDualWieldingSpeedBoost() {
        Mob mob = (Mob) (Object) this;
        boolean newState = MobAttackHelper.isDualWielding(mob);
        boolean currentState = this.dualWieldingAttributeMap != null;
        if (newState != currentState) {
            if (newState) {
                this.dualWieldingAttributeMap = HashMultimap.create();
                double multiplier = BetterCombat.config.dual_wielding_attack_speed_multiplier - 1.0F;
                this.dualWieldingAttributeMap.put(Attributes.ATTACK_SPEED, new AttributeModifier(dualWieldingSpeedModifierId, "Dual wielding attack speed boost", multiplier, AttributeModifier.Operation.MULTIPLY_BASE));
                mob.getAttributes().addTransientAttributeModifiers(this.dualWieldingAttributeMap);
            } else if (this.dualWieldingAttributeMap != null) {
                mob.getAttributes().removeAttributeModifiers(this.dualWieldingAttributeMap);
                this.dualWieldingAttributeMap = null;
            }
        }

    }

    @ModifyArg(
            method = {"doHurtTarget"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;getDamageBonus(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/MobType;)F"
            ),
            index = 0
    )
    public ItemStack getHeldItem(ItemStack heldItem) {
        Mob mob = (Mob) (Object) this;
        AttackHand currentHand = MobAttackHelper.getCurrentAttack(mob, this.comboCount);
        if (currentHand != null) {
            return currentHand.isOffHand() ? mob.getOffhandItem() : heldItem;
        } else {
            return heldItem;
        }
    }

    @WrapOperation(
            method = {"doHurtTarget"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    public ItemStack getMainHandStack_Redirect(Mob instance, Operation<ItemStack> original) {
        if (this.comboCount < 0) {
            return original.call(instance);
        } else {
            AttackHand hand = MobAttackHelper.getCurrentAttack(instance, this.comboCount);
            if (hand == null) {
                boolean isOffHand = MobAttackHelper.shouldAttackWithOffHand(instance, this.comboCount);
                return isOffHand ? ItemStack.EMPTY : original.call(instance);
            } else {
                this.lastAttack = hand;
                return hand.itemStack();
            }
        }
    }

    /*
    @Redirect(
            method = {"attack"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/player/PlayerEntity;setStackInHand(Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;)V"
            )
    )
    public void setStackInHand_Redirect(Player instance, InteractionHand handArg, ItemStack itemStack) {
        if (this.comboCount < 0) {
            instance.setItemInHand(handArg, itemStack);
        }

        AttackHand hand = this.lastAttack;
        if (hand == null) {
            hand = MobAttackHelper.getCurrentAttack(instance, this.comboCount);
        }

        if (hand == null) {
            instance.setItemInHand(handArg, itemStack);
        } else {
            InteractionHand redirectedHand = hand.isOffHand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            instance.setItemInHand(redirectedHand, itemStack);
        }
    }
     */

    @Override
    public void bettermobcombat$startUpswing(WeaponAttributes attributes) {
        AttackHand hand = this.getCurrentHand();
        if (hand != null) {
            float upswingRate = (float) hand.upswingRate();
            if (this.upswingTicks <= 0 && this.attackCooldown <= 0 && !this.isUsingItem() && !(this.bettercombat$getAttackStrengthScale(0.0F) < 1.0 - (double) upswingRate)) {
                this.releaseUsingItem();
                this.lastAttacked = 0;
                this.upswingStack = this.getMainHandItem();
                float attackCooldownTicksFloat = MobAttackHelper.getAttackCooldownTicksCapped(((Mob) (Object) this));
                int attackCooldownTicks = Math.round(attackCooldownTicksFloat);
                this.comboReset = Math.round(attackCooldownTicksFloat * BetterCombat.config.combo_reset_rate);
                this.upswingTicks = Math.max(Math.round(attackCooldownTicksFloat * upswingRate), 1);
                this.lastSwingDuration = attackCooldownTicksFloat;
                this.setAttackCooldown(attackCooldownTicks + BetterMobCombatCommon.getServerConfig().mob_additional_attack_cooldown);
                String animationName = hand.attack().animation();
                boolean isOffHand = hand.isOffHand();
                AnimatedHand animatedHand = AnimatedHand.from(isOffHand, attributes.isTwoHanded());
                Services.PLATFORM.playMobAttackAnimation(this, animatedHand, animationName, attackCooldownTicksFloat, upswingRate);
                BetterMobCombatClientEvents.ATTACK_START.invoke((handler) -> {
                    handler.onMobAttackStart(((Mob)(Object)this), hand);
                });
            }
        } else{
            Constants.LOG.error("Upswing did not start for {} due to lack of AttackHand", this);
        }
    }

    @Unique
    private void cancelSwingIfNeeded() {
        if (this.upswingStack != null && !areItemStackEqual(this.getMainHandItem(), this.upswingStack)) {
            this.cancelWeaponSwing();
        }
    }

    @Unique
    private void attackFromUpswingIfNeeded() {
        if (this.upswingTicks > 0) {
            --this.upswingTicks;
            if (this.upswingTicks == 0) {
                this.performAttack();
                this.upswingStack = null;
            }
        }

    }

    @Unique
    private void resetComboIfNeeded() {
        if (this.lastAttacked > this.comboReset && this.getComboCount() > 0) {
            this.setComboCount(0);
        }

        if (!MobAttackHelper.shouldAttackWithOffHand(((Mob) (Object) this), this.getComboCount()) && (this.getMainHandItem() == null || this.lastAttackedWithItemStack != null && !this.lastAttackedWithItemStack.getItem().equals(this.getMainHandItem().getItem()))) {
            this.setComboCount(0);
        }

    }

    @Unique
    private boolean shouldUpdateTargetsInReach() {
        return !this.level().isClientSide && this.getTarget() != null && this.targetsInReach == null;
    }

    @Unique
    private void updateTargetsInReach(List<Entity> targets) {
        this.targetsInReach = targets;
    }

    @Unique
    private void updateTargetsIfNeeded() {
        if (this.shouldUpdateTargetsInReach()) {
            AttackHand hand = MobAttackHelper.getCurrentAttack(this, this.getComboCount());
            WeaponAttributes attributes = WeaponRegistry.getAttributes(this.getMainHandItem());
            List<Entity> targets = List.of();
            if (attributes != null && attributes.attacks() != null) {
                targets = MobTargetFinder.findAttackTargets(((Mob) (Object) this), this.getTarget(), hand.attack(), attributes.attackRange());
            }

            this.updateTargetsInReach(targets);
        }

    }

    @Inject(
            method = {"tick"},
            at = {@At("HEAD")}
    )
    private void pre_Tick(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            if (this.attackCooldown > 0) {
                --this.attackCooldown;
            }
            this.targetsInReach = null;
            ++this.lastAttacked;
            this.cancelSwingIfNeeded();
            this.attackFromUpswingIfNeeded();
            this.updateTargetsIfNeeded();
            this.resetComboIfNeeded();
        }
    }

    @Unique
    private void performAttack() {
        AttackHand hand = this.getCurrentHand();
        if (hand != null) {
            WeaponAttributes.Attack attack = hand.attack();
            double upswingRate = hand.upswingRate();
            if (!(this.bettercombat$getAttackStrengthScale(0.0F) < 1.0 - upswingRate)) {
                Entity cursorTarget = this.getTarget();
                List<Entity> targets = MobTargetFinder.findAttackTargets(((Mob) (Object) this), cursorTarget, attack, hand.attributes().attackRange());
                this.updateTargetsInReach(targets);
                if (targets.size() == 0) {
                    Constants.LOG.warn("Mob {} executed an attack with no targets in range", this);
                    // PlatformClient.onEmptyLeftClick(((Mob)(Object)this));
                }

                MobCombatHelper.processAttack(this.level(), ((Mob) (Object) this), this.getComboCount(), targets);

                this.bettercombat$resetAttackStrengthTicker();
                BetterMobCombatClientEvents.ATTACK_HIT.invoke((handler) -> {
                    handler.onMobAttackHit(((Mob)(Object)this), hand, targets, cursorTarget);
                });
                this.setComboCount(this.getComboCount() + 1);
                if (!hand.isOffHand()) {
                    this.lastAttackedWithItemStack = hand.itemStack();
                }

            }
        }
    }

    @Unique
    private AttackHand getCurrentHand() {
        return MobAttackHelper.getCurrentAttack(((Mob) (Object) this), this.getComboCount());
    }

    @Unique
    private static boolean areItemStackEqual(ItemStack left, ItemStack right) {
        if (left == null && right == null) {
            return true;
        } else {
            return left != null && right != null ? ItemStack.matches(left, right) : false;
        }
    }

    @Override
    public int bettermobcombat$getAttackCooldown(){
        return this.attackCooldown;
    }

    @Unique
    private void setAttackCooldown(int ticks) {
        this.attackCooldown = ticks;
    }

    @Unique
    private void cancelWeaponSwing() {
        int downWind = (int) Math.round((double) MobAttackHelper.getAttackCooldownTicksCapped(((Mob) (Object) this)) * (1.0 - 0.5 * (double) BetterCombat.config.upswing_multiplier));
        Services.PLATFORM.stopMobAttackAnimation(this, downWind);
        this.upswingStack = null;
        this.setAttackCooldown(0);
    }

    @Unique
    public boolean hasTargetsInReach() {
        return this.targetsInReach != null && !this.targetsInReach.isEmpty();
    }

    @Override
    public float bettermobcombat$getSwingProgress() {
        return !((float) this.lastAttacked > this.lastSwingDuration) && !(this.lastSwingDuration <= 0.0F) ? (float) this.lastAttacked / this.lastSwingDuration : 1.0F;
    }

    @Override
    public int bettermobcombat$getUpswingTicks() {
        return this.upswingTicks;
    }

    @Override
    public void bettermobcombat$cancelUpswing() {
        if (this.upswingTicks > 0) {
            this.cancelWeaponSwing();
        }

    }

    @Override
    public int getComboCount() {
        return this.comboCount;
    }

    @Override
    public void setComboCount(int comboCount) {
        this.comboCount = comboCount;
    }

    @Inject(method = "isWithinMeleeAttackRange", at = @At("HEAD"), cancellable = true)
    private void pre_isWithinMeleeAttackRange(LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingAnimatedAttackWeapon((Mob) (Object)this, (m, wa) -> {
            AttackHand currentAttack = this.getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange()));
            }
        });
    }
}
