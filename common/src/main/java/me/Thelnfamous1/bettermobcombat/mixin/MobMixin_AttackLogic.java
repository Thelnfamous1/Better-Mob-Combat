package me.Thelnfamous1.bettermobcombat.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.Thelnfamous1.bettermobcombat.BetterMobCombat;
import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.api.MobAttackStrength;
import me.Thelnfamous1.bettermobcombat.api.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.client.BetterMobCombatEvents;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import me.Thelnfamous1.bettermobcombat.logic.MobCombatHelper;
import me.Thelnfamous1.bettermobcombat.logic.MobTargetFinder;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.animation.PlayerAttackAnimatable;
import net.bettercombat.logic.AnimatedHand;
import net.bettercombat.logic.PlayerAttackProperties;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.utils.MathHelper;
import net.minecraft.util.Mth;
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

@Mixin(value = Mob.class)
public abstract class MobMixin_AttackLogic extends LivingEntity implements EntityPlayer_BetterCombat, MobAttackStrength, MobAttackWindup, PlayerAttackProperties {

    @Unique
    private int bettermobcombat$comboCount = 0;
    @Unique
    private Multimap<Attribute, AttributeModifier> bettermobcombat$dualWieldingAttributeMap;
    @Unique
    private static final UUID bettermobcombat$DUAL_WIELDING_SPEED_MODIFIER_ID = UUID.fromString("6b364332-0dc4-11ed-861d-0242ac120002");
    @Unique
    private AttackHand bettermobcombat$lastAttack;
    @Unique
    private ItemStack bettermobcombat$lastItemInMainHand = ItemStack.EMPTY;
    @Unique
    private int bettermobcombat$attackCooldown;

    @Shadow
    @Nullable
    public abstract LivingEntity getTarget();

    @Unique
    private ItemStack bettermobcombat$upswingStack;
    @Unique
    private ItemStack bettermobcombat$lastAttackedWithItemStack;
    @Unique
    private int bettermobcombat$upswingTicks = 0;
    @Unique
    private int bettermobcombat$lastAttacked = 1000;
    @Unique
    private float bettermobcombat$lastSwingDuration = 0.0F;
    @Unique
    private int bettermobcombat$comboReset = 0;
    @Unique
    private List<Entity> bettermobcombat$targetsInReach = null;

    protected MobMixin_AttackLogic(EntityType<? extends LivingEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public @Nullable AttackHand getCurrentAttack() {
        if (this.bettermobcombat$comboCount < 0) {
            return null;
        } else {
            Mob player = (Mob) (Object) this;
            return MobAttackHelper.getCurrentAttack(player, this.bettermobcombat$comboCount);
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

    @Inject(
            method = {"tick"},
            at = {@At("TAIL")}
    )
    public void post_Tick(CallbackInfo ci) {
        if (this.level().isClientSide) {
            ((PlayerAttackAnimatable) this).updateAnimationsOnTick();
        }

        this.bettermobcombat$updateDualWieldingSpeedBoost();

        // Copy of attack strength logic in Player#tick
        ++this.attackStrengthTicker;
        ItemStack itemstack = this.getMainHandItem();
        if (!ItemStack.matches(this.bettermobcombat$lastItemInMainHand, itemstack)) {
            if (!ItemStack.isSameItem(this.bettermobcombat$lastItemInMainHand, itemstack)) {
                this.bettercombat$resetAttackStrengthTicker();
            }

            this.bettermobcombat$lastItemInMainHand = itemstack.copy();
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
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(this)){
            return;
        }
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
    private void bettermobcombat$updateDualWieldingSpeedBoost() {
        Mob mob = (Mob) (Object) this;
        boolean newState = MobAttackHelper.isDualWielding(mob);
        boolean currentState = this.bettermobcombat$dualWieldingAttributeMap != null;
        if (newState != currentState) {
            if (newState) {
                this.bettermobcombat$dualWieldingAttributeMap = HashMultimap.create();
                double multiplier = BetterCombat.config.dual_wielding_attack_speed_multiplier - 1.0F;
                this.bettermobcombat$dualWieldingAttributeMap.put(Attributes.ATTACK_SPEED, new AttributeModifier(bettermobcombat$DUAL_WIELDING_SPEED_MODIFIER_ID, "Dual wielding attack speed boost", multiplier, AttributeModifier.Operation.MULTIPLY_BASE));
                mob.getAttributes().addTransientAttributeModifiers(this.bettermobcombat$dualWieldingAttributeMap);
            } else if (this.bettermobcombat$dualWieldingAttributeMap != null) {
                mob.getAttributes().removeAttributeModifiers(this.bettermobcombat$dualWieldingAttributeMap);
                this.bettermobcombat$dualWieldingAttributeMap = null;
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
        AttackHand currentHand = MobAttackHelper.getCurrentAttack(mob, this.bettermobcombat$comboCount);
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
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(this)){
            return original.call(instance);
        }
        if (this.bettermobcombat$comboCount < 0) {
            return original.call(instance);
        } else {
            AttackHand hand = MobAttackHelper.getCurrentAttack(instance, this.bettermobcombat$comboCount);
            if (hand == null) {
                boolean isOffHand = MobAttackHelper.shouldAttackWithOffHand(instance, this.bettermobcombat$comboCount);
                return isOffHand ? ItemStack.EMPTY : original.call(instance);
            } else {
                this.bettermobcombat$lastAttack = hand;
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
        AttackHand hand = this.bettermobcombat$getCurrentHand();
        if (hand != null) {
            float upswingRate = (float) hand.upswingRate();
            if (this.bettermobcombat$upswingTicks <= 0 && this.bettermobcombat$attackCooldown <= 0 && !this.isUsingItem() && !(this.bettercombat$getAttackStrengthScale(0.0F) < 1.0 - (double) upswingRate)) {
                this.releaseUsingItem();
                this.bettermobcombat$lastAttacked = 0;
                this.bettermobcombat$upswingStack = this.getMainHandItem();
                float attackCooldownTicksFloat = MobAttackHelper.getAttackCooldownTicksCapped(((Mob) (Object) this));
                int attackCooldownTicks = Math.round(attackCooldownTicksFloat);
                this.bettermobcombat$comboReset = Math.round(attackCooldownTicksFloat * BetterCombat.config.combo_reset_rate);
                this.bettermobcombat$upswingTicks = Math.max(Math.round(attackCooldownTicksFloat * upswingRate), 1);
                this.bettermobcombat$lastSwingDuration = attackCooldownTicksFloat;
                this.bettermobcombat$setAttackCooldown(attackCooldownTicks + BetterMobCombat.getServerConfig().mob_additional_attack_cooldown);
                String animationName = hand.attack().animation();
                boolean isOffHand = hand.isOffHand();
                AnimatedHand animatedHand = AnimatedHand.from(isOffHand, attributes.isTwoHanded());
                Services.PLATFORM.playMobAttackAnimation(this, animatedHand, animationName, attackCooldownTicksFloat, upswingRate);
                BetterMobCombatEvents.ATTACK_START.invoke((handler) -> {
                    handler.onMobAttackStart(((Mob)(Object)this), hand);
                });
            }
        } else{
            Constants.LOG.error("Upswing did not start for {} due to lack of AttackHand", this);
        }
    }

    @Unique
    private void bettermobcombat$cancelSwingIfNeeded() {
        if (this.bettermobcombat$upswingStack != null && !bettermobcombat$areItemStackEqual(this.getMainHandItem(), this.bettermobcombat$upswingStack)) {
            this.bettermobcombat$cancelWeaponSwing();
        }
    }

    @Unique
    private void bettermobcombat$attackFromUpswingIfNeeded() {
        if (this.bettermobcombat$upswingTicks > 0) {
            --this.bettermobcombat$upswingTicks;
            if (this.bettermobcombat$upswingTicks == 0) {
                this.bettermobcombat$performAttack();
                this.bettermobcombat$upswingStack = null;
            }
        }

    }

    @Unique
    private void bettermobcombat$resetComboIfNeeded() {
        if (this.bettermobcombat$lastAttacked > this.bettermobcombat$comboReset && this.getComboCount() > 0) {
            this.setComboCount(0);
        }

        if (!MobAttackHelper.shouldAttackWithOffHand(((Mob) (Object) this), this.getComboCount()) && (this.getMainHandItem() == null || this.bettermobcombat$lastAttackedWithItemStack != null && !this.bettermobcombat$lastAttackedWithItemStack.getItem().equals(this.getMainHandItem().getItem()))) {
            this.setComboCount(0);
        }

    }

    @Unique
    private boolean bettermobcombat$shouldUpdateTargetsInReach() {
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(this)){
            return false;
        }
        return !this.level().isClientSide && this.getTarget() != null && this.bettermobcombat$targetsInReach == null;
    }

    @Unique
    private void bettermobcombat$updateTargetsInReach(List<Entity> targets) {
        this.bettermobcombat$targetsInReach = targets;
    }

    @Unique
    private void bettermobcombat$updateTargetsIfNeeded() {
        if (this.bettermobcombat$shouldUpdateTargetsInReach()) {
            AttackHand hand = MobAttackHelper.getCurrentAttack(this, this.getComboCount());
            WeaponAttributes attributes = WeaponRegistry.getAttributes(this.getMainHandItem());
            List<Entity> targets = List.of();
            if (attributes != null && attributes.attacks() != null) {
                targets = MobTargetFinder.findAttackTargets(((Mob) (Object) this), this.getTarget(), hand.attack(), attributes.attackRange());
            }

            this.bettermobcombat$updateTargetsInReach(targets);
        }

    }

    @Inject(
            method = {"tick"},
            at = {@At("HEAD")}
    )
    private void pre_Tick(CallbackInfo ci) {
        if (!this.level().isClientSide) {
            if (this.bettermobcombat$attackCooldown > 0) {
                --this.bettermobcombat$attackCooldown;
            }
            this.bettermobcombat$targetsInReach = null;
            ++this.bettermobcombat$lastAttacked;
            this.bettermobcombat$cancelSwingIfNeeded();
            this.bettermobcombat$attackFromUpswingIfNeeded();
            this.bettermobcombat$updateTargetsIfNeeded();
            this.bettermobcombat$resetComboIfNeeded();
        }
    }

    @Unique
    private void bettermobcombat$performAttack() {
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(this)){
            return;
        }
        AttackHand hand = this.bettermobcombat$getCurrentHand();
        if (hand != null) {
            WeaponAttributes.Attack attack = hand.attack();
            double upswingRate = hand.upswingRate();
            if (!(this.bettercombat$getAttackStrengthScale(0.0F) < 1.0 - upswingRate)) {
                Entity intendedTarget = this.getTarget();
                List<Entity> targets = MobTargetFinder.findAttackTargets(((Mob) (Object) this), null, attack, hand.attributes().attackRange());
                this.bettermobcombat$updateTargetsInReach(targets);
                if (intendedTarget == null && targets.size() == 0) {
                    Constants.LOG.debug("Mob {} executed an attack with no AI attack target and no targets in range", this);
                    // PlatformClient.onEmptyLeftClick(((Mob)(Object)this));
                }

                MobCombatHelper.processAttack(this.level(), ((Mob) (Object) this), this.getComboCount(), targets);

                this.bettercombat$resetAttackStrengthTicker();
                BetterMobCombatEvents.ATTACK_HIT.invoke((handler) -> {
                    handler.onMobAttackHit(((Mob)(Object)this), hand, targets, intendedTarget);
                });
                this.setComboCount(this.getComboCount() + 1);
                if (!hand.isOffHand()) {
                    this.bettermobcombat$lastAttackedWithItemStack = hand.itemStack();
                }

            }
        }
    }

    @Unique
    @Nullable
    private AttackHand bettermobcombat$getCurrentHand() {
        return MobAttackHelper.getCurrentAttack(((Mob) (Object) this), this.getComboCount());
    }

    @Unique
    private static boolean bettermobcombat$areItemStackEqual(ItemStack left, ItemStack right) {
        if (left == null && right == null) {
            return true;
        } else {
            return left != null && right != null ? ItemStack.matches(left, right) : false;
        }
    }

    @Override
    public int bettermobcombat$getAttackCooldown(){
        return this.bettermobcombat$attackCooldown;
    }

    @Unique
    private void bettermobcombat$setAttackCooldown(int ticks) {
        this.bettermobcombat$attackCooldown = ticks;
    }

    @Unique
    private void bettermobcombat$cancelWeaponSwing() {
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(this)){
            return;
        }
        int downWind = (int) Math.round((double) MobAttackHelper.getAttackCooldownTicksCapped(((Mob) (Object) this)) * (1.0 - 0.5 * (double) BetterCombat.config.upswing_multiplier));
        Services.PLATFORM.stopMobAttackAnimation(this, downWind);
        this.bettermobcombat$upswingStack = null;
        this.bettermobcombat$setAttackCooldown(0);
    }

    @Unique
    public boolean bettermobcombat$hasTargetsInReach() {
        return this.bettermobcombat$targetsInReach != null && !this.bettermobcombat$targetsInReach.isEmpty();
    }

    @Override
    public float bettermobcombat$getSwingProgress() {
        return !((float) this.bettermobcombat$lastAttacked > this.bettermobcombat$lastSwingDuration) && !(this.bettermobcombat$lastSwingDuration <= 0.0F) ? (float) this.bettermobcombat$lastAttacked / this.bettermobcombat$lastSwingDuration : 1.0F;
    }

    @Override
    public int bettermobcombat$getUpswingTicks() {
        return this.bettermobcombat$upswingTicks;
    }

    @Override
    public void bettermobcombat$cancelUpswing() {
        if (this.bettermobcombat$upswingTicks > 0) {
            this.bettermobcombat$cancelWeaponSwing();
        }

    }

    @Override
    public int getComboCount() {
        return this.bettermobcombat$comboCount;
    }

    @Override
    public void setComboCount(int comboCount) {
        if(!this.level().isClientSide && this.bettermobcombat$comboCount != comboCount){
            Services.PLATFORM.syncMobComboCount(this, comboCount);
        }
        this.bettermobcombat$comboCount = comboCount;
    }

    @Inject(method = "isWithinMeleeAttackRange", at = @At("HEAD"), cancellable = true)
    private void pre_isWithinMeleeAttackRange(LivingEntity target, CallbackInfoReturnable<Boolean> cir){
        MobCombatHelper.onHoldingBetterCombatWeapon((Mob) (Object)this, (m, wa) -> {
            AttackHand currentAttack = this.getCurrentAttack();
            if(currentAttack != null){
                cir.setReturnValue(MobCombatHelper.isWithinAttackRange(m, target, currentAttack.attack(), wa.attackRange()));
            }
        });
    }

    @Inject(
            method = {"aiStep"},
            at = {@At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;aiStep()V"
            )}
    )
    private void tickMovement_ModifyInput(CallbackInfo ci) {
        if(BetterMobCombat.getServerConfigHelper().isBlacklistedForBetterCombat(this)){
            return;
        }
        double multiplier = Math.min(Math.max(BetterCombat.config.movement_speed_while_attacking, 0.0), 1.0);
        if (multiplier != 1.0) {
            if (!this.isPassenger() || BetterCombat.config.movement_speed_effected_while_mounting) {
                float swingProgress = this.bettermobcombat$getSwingProgress();
                if ((double)swingProgress < 0.98) {
                    if (BetterCombat.config.movement_speed_applied_smoothly) {
                        double p2;
                        if ((double)swingProgress <= 0.5) {
                            p2 = MathHelper.easeOutCubic(swingProgress * 2.0F);
                        } else {
                            p2 = MathHelper.easeOutCubic(1.0 - ((double)swingProgress - 0.5) * 2.0);
                        }

                        multiplier = (float)(1.0 - (1.0 - multiplier) * p2);
                    }

                    this.zza *= multiplier;
                    this.xxa *= multiplier;
                }

            }
        }
    }
}
