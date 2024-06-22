package me.Thelnfamous1.bettermobcombat.mixin;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.Thelnfamous1.bettermobcombat.duck.MobDuck;
import me.Thelnfamous1.bettermobcombat.logic.MobAttackHelper;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.EntityPlayer_BetterCombat;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.client.animation.PlayerAttackAnimatable;
import net.bettercombat.logic.PlayerAttackProperties;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;

@Mixin(Mob.class)
public abstract class MobMixin extends LivingEntity implements MobDuck, PlayerAttackProperties, EntityPlayer_BetterCombat {

    @Unique
    private int comboCount = 0;
    @Unique
    private Multimap<Attribute, AttributeModifier> dualWieldingAttributeMap;
    @Unique
    private static final UUID dualWieldingSpeedModifierId = UUID.fromString("6b364332-0dc4-11ed-861d-0242ac120002");
    @Unique
    private AttackHand lastAttack;

    protected MobMixin(EntityType<? extends LivingEntity> $$0, Level $$1) {
        super($$0, $$1);
    }

    @Override
    public int getComboCount() {
        return this.comboCount;
    }

    @Override
    public void setComboCount(int comboCount) {
        this.comboCount = comboCount;
    }

    @Override
    public float bettermobcombat$getCurrentItemAttackStrengthDelay() {
        double attackSpeed = 4;
        if(this.getAttribute(Attributes.ATTACK_SPEED) != null){
            attackSpeed = this.getAttributeValue(Attributes.ATTACK_SPEED);
        }
        return (float)(1.0D / attackSpeed * 20.0D);
    }

    @Inject(
            method = {"tick"},
            at = {@At("TAIL")}
    )
    public void post_Tick(CallbackInfo ci) {
        if (this.level().isClientSide()) {
            ((PlayerAttackAnimatable)this).updateAnimationsOnTick();
        }

        this.updateDualWieldingSpeedBoost();
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
        ItemStack mainHandStack = MobAttackHelper.getDirectMainhand((Mob)(Object)this);
        WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(mainHandStack);
        if (mainHandAttributes != null && mainHandAttributes.isTwoHanded()) {
            mainHandHasTwoHanded = true;
        }

        boolean offHandHasTwoHanded = false;
        ItemStack offHandStack = MobAttackHelper.getDirectOffhand((Mob)(Object)this);
        WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(offHandStack);
        if (offHandAttributes != null && offHandAttributes.isTwoHanded()) {
            offHandHasTwoHanded = true;
        }

        if (slot == EquipmentSlot.OFFHAND && (mainHandHasTwoHanded || offHandHasTwoHanded)) {
            cir.setReturnValue(ItemStack.EMPTY);
            cir.cancel();
        }
    }

    private void updateDualWieldingSpeedBoost() {
        Mob mob = (Mob)(Object)this;
        boolean newState = MobAttackHelper.isDualWielding(mob);
        boolean currentState = this.dualWieldingAttributeMap != null;
        if (newState != currentState) {
            if (newState) {
                this.dualWieldingAttributeMap = HashMultimap.create();
                double multiplier = (double)(BetterCombat.config.dual_wielding_attack_speed_multiplier - 1.0F);
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
        Mob mob = (Mob)(Object)this;
        AttackHand currentHand = MobAttackHelper.getCurrentAttack(mob, this.comboCount);
        if (currentHand != null) {
            return currentHand.isOffHand() ? mob.getOffhandItem() : heldItem;
        } else {
            return heldItem;
        }
    }

    @Redirect(
            method = {"doHurtTarget"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/Mob;getMainHandItem()Lnet/minecraft/world/item/ItemStack;"
            )
    )
    public ItemStack getMainHandStack_Redirect(Mob instance) {
        if (this.comboCount < 0) {
            return instance.getMainHandItem();
        } else {
            AttackHand hand = MobAttackHelper.getCurrentAttack(instance, this.comboCount);
            if (hand == null) {
                boolean isOffHand = MobAttackHelper.shouldAttackWithOffHand(instance, this.comboCount);
                return isOffHand ? ItemStack.EMPTY : instance.getMainHandItem();
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
    public @Nullable AttackHand getCurrentAttack() {
        if (this.comboCount < 0) {
            return null;
        } else {
            Mob player = (Mob)(Object)this;
            return MobAttackHelper.getCurrentAttack(player, this.comboCount);
        }
    }
}
