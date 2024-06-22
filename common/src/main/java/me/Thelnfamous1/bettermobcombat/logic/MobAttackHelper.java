package me.Thelnfamous1.bettermobcombat.logic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.duck.MobAttackStrength;
import me.Thelnfamous1.bettermobcombat.duck.MobAttackWindup;
import me.Thelnfamous1.bettermobcombat.mixin.MobAccessor;
import me.Thelnfamous1.bettermobcombat.mixin.ServerNetworkAccessor;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.ComboState;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.PlayerAttackProperties;
import net.bettercombat.logic.TargetHelper;
import net.bettercombat.logic.WeaponRegistry;
import net.bettercombat.logic.knockback.ConfigurableKnockback;
import net.bettercombat.mixin.LivingEntityAccessor;
import net.bettercombat.utils.MathHelper;
import net.bettercombat.utils.SoundHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Arrays;
import java.util.List;

public class MobAttackHelper {
    private static final Object attributesLock = new Object();

    public MobAttackHelper() {
    }

    public static float getDualWieldingAttackDamageMultiplier(LivingEntity mob, AttackHand hand) {
        return isDualWielding(mob) ? (hand.isOffHand() ? BetterCombat.config.dual_wielding_off_hand_damage_multiplier : BetterCombat.config.dual_wielding_main_hand_damage_multiplier) : 1.0F;
    }

    public static boolean shouldAttackWithOffHand(LivingEntity mob, int comboCount) {
        return isDualWielding(mob) && comboCount % 2 == 1;
    }

    public static boolean isDualWielding(LivingEntity mob) {
        WeaponAttributes mainAttributes = WeaponRegistry.getAttributes(mob.getMainHandItem());
        WeaponAttributes offAttributes = WeaponRegistry.getAttributes(mob.getOffhandItem());
        return mainAttributes != null && !mainAttributes.isTwoHanded() && offAttributes != null && !offAttributes.isTwoHanded();
    }

    public static boolean isTwoHandedWielding(LivingEntity mob) {
        WeaponAttributes mainAttributes = WeaponRegistry.getAttributes(mob.getMainHandItem());
        return mainAttributes != null && mainAttributes.isTwoHanded();
    }

    public static float getAttackCooldownTicksCapped(Mob mob) {
        return Math.max(((MobAttackStrength)mob).bettermobcombat$getCurrentItemAttackStrengthDelay(), (float)BetterCombat.config.attack_interval_cap);
    }

    public static AttackHand getCurrentAttack(LivingEntity mob, int comboCount) {
        if (isDualWielding(mob)) {
            boolean isOffHand = shouldAttackWithOffHand(mob, comboCount);
            ItemStack itemStack = isOffHand ? mob.getOffhandItem() : mob.getMainHandItem();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            if (attributes != null && attributes.attacks() != null) {
                int handSpecificComboCount = (isOffHand && comboCount > 0 ? comboCount - 1 : comboCount) / 2;
                AttackSelection attackSelection = selectAttack(handSpecificComboCount, attributes, mob, isOffHand);
                WeaponAttributes.Attack attack = attackSelection.attack;
                ComboState combo = attackSelection.comboState;
                return new AttackHand(attack, combo, isOffHand, attributes, itemStack);
            }
        } else {
            ItemStack itemStack = mob.getMainHandItem();
            WeaponAttributes attributes = WeaponRegistry.getAttributes(itemStack);
            if (attributes != null && attributes.attacks() != null) {
                AttackSelection attackSelection = selectAttack(comboCount, attributes, mob, false);
                WeaponAttributes.Attack attack = attackSelection.attack;
                ComboState combo = attackSelection.comboState;
                return new AttackHand(attack, combo, false, attributes, itemStack);
            }
        }

        return null;
    }

    private static AttackSelection selectAttack(int comboCount, WeaponAttributes attributes, LivingEntity mob, boolean isOffHandAttack) {
        WeaponAttributes.Attack[] attacks = attributes.attacks();
        attacks = Arrays.stream(attacks).filter((attack) -> attack.conditions() == null || attack.conditions().length == 0 || evaluateConditions(attack.conditions(), mob, isOffHandAttack)).toArray(WeaponAttributes.Attack[]::new);
        if (comboCount < 0) {
            comboCount = 0;
        }

        int index = comboCount % attacks.length;
        return new AttackSelection(attacks[index], new ComboState(index + 1, attacks.length));
    }

    private static boolean evaluateConditions(WeaponAttributes.Condition[] conditions, LivingEntity mob, boolean isOffHandAttack) {
        return Arrays.stream(conditions).allMatch((condition) -> evaluateCondition(condition, mob, isOffHandAttack));
    }

    private static boolean evaluateCondition(WeaponAttributes.Condition condition, LivingEntity mob, boolean isOffHandAttack) {
        if (condition == null) {
            return true;
        } else {
            ItemStack offhandStack;
            switch (condition) {
                case NOT_DUAL_WIELDING:
                    return !isDualWielding(mob);
                case DUAL_WIELDING_ANY:
                    return isDualWielding(mob);
                case DUAL_WIELDING_SAME:
                    return isDualWielding(mob) && mob.getMainHandItem().getItem() == mob.getOffhandItem().getItem();
                case DUAL_WIELDING_SAME_CATEGORY:
                    if (!isDualWielding(mob)) {
                        return false;
                    } else {
                        WeaponAttributes mainHandAttributes = WeaponRegistry.getAttributes(mob.getMainHandItem());
                        WeaponAttributes offHandAttributes = WeaponRegistry.getAttributes(mob.getOffhandItem());
                        if (mainHandAttributes.category() != null && !mainHandAttributes.category().isEmpty() && offHandAttributes.category() != null && !offHandAttributes.category().isEmpty()) {
                            return mainHandAttributes.category().equals(offHandAttributes.category());
                        }

                        return false;
                    }
                case NO_OFFHAND_ITEM:
                    offhandStack = mob.getOffhandItem();
                    if (offhandStack != null && !offhandStack.isEmpty()) {
                        return false;
                    }

                    return true;
                case OFF_HAND_SHIELD:
                    offhandStack = mob.getOffhandItem();
                    if (offhandStack == null && !(offhandStack.getItem() instanceof ShieldItem)) {
                        return false;
                    }

                    return true;
                case MAIN_HAND_ONLY:
                    return !isOffHandAttack;
                case OFF_HAND_ONLY:
                    return isOffHandAttack;
                case MOUNTED:
                    return mob.getVehicle() != null;
                case NOT_MOUNTED:
                    return mob.getVehicle() == null;
                default:
                    return true;
            }
        }
    }

    public static void offhandAttributes(LivingEntity mob, Runnable runnable) {
        synchronized(attributesLock) {
            setAttributesForOffHandAttack(mob, true);
            runnable.run();
            setAttributesForOffHandAttack(mob, false);
        }
    }

    public static void setAttributesForOffHandAttack(LivingEntity mob, boolean useOffHand) {
        ItemStack mainHandStack = mob.getMainHandItem();
        ItemStack offHandStack = mob.getOffhandItem();
        ItemStack add;
        ItemStack remove;
        if (useOffHand) {
            remove = mainHandStack;
            add = offHandStack;
        } else {
            remove = offHandStack;
            add = mainHandStack;
        }

        if (remove != null) {
            mob.getAttributes().removeAttributeModifiers(remove.getAttributeModifiers(EquipmentSlot.MAINHAND));
        }

        if (add != null) {
            mob.getAttributes().addTransientAttributeModifiers(add.getAttributeModifiers(EquipmentSlot.MAINHAND));
        }

    }

    public static ItemStack getDirectMainhand(Mob mob){
        return ((MobAccessor)mob).bettermobcombat$getHandItems().get(EquipmentSlot.MAINHAND.getIndex());
    }

    public static ItemStack getDirectOffhand(Mob mob){
        return ((MobAccessor)mob).bettermobcombat$getHandItems().get(EquipmentSlot.OFFHAND.getIndex());
    }

    public static boolean doAttackWithWeapon(Mob mob) {
        WeaponAttributes attributes = WeaponRegistry.getAttributes(mob.getMainHandItem());
        if (attributes != null && attributes.attacks() != null) {
            ((MobAttackWindup)mob).bettermobcombat$startUpswing(attributes);
            return true;
        }
        return false;
    }

    public static void processAttack(Level world, Mob mob, int comboCount, List<Entity> targets){
        if (world != null && !world.isClientSide) {
            AttackHand hand = getCurrentAttack(mob, comboCount);
            if (hand == null) {
                Constants.LOG.error("Server handling attack for {} - No current attack hand!", mob);
                Constants.LOG.error("Combo count: " + comboCount + " is dual wielding: " + isDualWielding(mob));
                Constants.LOG.error("Main-hand stack: " + mob.getMainHandItem());
                Constants.LOG.error("Off-hand stack: " + mob.getOffhandItem());
            } else {
                WeaponAttributes.Attack attack = hand.attack();
                WeaponAttributes attributes = hand.attributes();
                world.getServer().executeIfPossible(() -> {
                    ((PlayerAttackProperties)mob).setComboCount(comboCount);
                    Multimap<Attribute, AttributeModifier> comboAttributes = null;
                    Multimap<Attribute, AttributeModifier> dualWieldingAttributes = null;
                    Multimap<Attribute, AttributeModifier> sweepingModifiers = HashMultimap.create();
                    int sweepingLevel;
                    if (attributes != null && attack != null) {
                        comboAttributes = HashMultimap.create();
                        double comboMultiplier = attack.damageMultiplier() - 1.0;
                        comboAttributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ServerNetworkAccessor.bettermobcombat$getCOMBO_DAMAGE_MODIFIER_ID(), "COMBO_DAMAGE_MULTIPLIER", comboMultiplier, AttributeModifier.Operation.MULTIPLY_BASE));
                        mob.getAttributes().addTransientAttributeModifiers(comboAttributes);
                        float dualWieldingMultiplier = getDualWieldingAttackDamageMultiplier(mob, hand) - 1.0F;
                        if (dualWieldingMultiplier != 0.0F) {
                            dualWieldingAttributes = HashMultimap.create();
                            dualWieldingAttributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ServerNetworkAccessor.bettermobcombat$getDUAL_WIELDING_MODIFIER_ID(), "DUAL_WIELDING_DAMAGE_MULTIPLIER", (double)dualWieldingMultiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
                            mob.getAttributes().addTransientAttributeModifiers(dualWieldingAttributes);
                        }

                        if (hand.isOffHand()) {
                            setAttributesForOffHandAttack(mob, true);
                        }

                        SoundHelper.playSound((ServerLevel) world, mob, attack.swingSound());
                        if (BetterCombat.config.allow_reworked_sweeping && targets.size() > 1) {
                            double multiplier = 1.0 - (double)(BetterCombat.config.reworked_sweeping_maximum_damage_penalty / (float)BetterCombat.config.reworked_sweeping_extra_target_count * (float)Math.min(BetterCombat.config.reworked_sweeping_extra_target_count, targets.size() - 1));
                            sweepingLevel = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SWEEPING_EDGE, hand.itemStack());
                            double sweepingSteps = (double)BetterCombat.config.reworked_sweeping_enchant_restores / (double)Enchantments.SWEEPING_EDGE.getMaxLevel();
                            multiplier += (double)sweepingLevel * sweepingSteps;
                            multiplier = Math.min(multiplier, 1.0);
                            sweepingModifiers.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ServerNetworkAccessor.bettermobcombat$getSWEEPING_MODIFIER_ID(), "SWEEPING_DAMAGE_MODIFIER", multiplier - 1.0, AttributeModifier.Operation.MULTIPLY_TOTAL));
                            mob.getAttributes().addTransientAttributeModifiers(sweepingModifiers);
                            boolean playEffects = !BetterCombat.config.reworked_sweeping_sound_and_particles_only_for_swords || hand.itemStack().getItem() instanceof SwordItem;
                            if (BetterCombat.config.reworked_sweeping_plays_sound && playEffects) {
                                world.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, mob.getSoundSource(), 1.0F, 1.0F);
                            }

                            if (BetterCombat.config.reworked_sweeping_emits_particles && playEffects) {
                                sweepAttack(mob);
                            }
                        }
                    }

                    float attackCooldown = getAttackCooldownTicksCapped(mob);
                    float knockbackMultiplier = BetterCombat.config.knockback_reduced_for_fast_attacks ? MathHelper.clamp(attackCooldown / 12.5F, 0.1F, 1.0F) : 1.0F;
                    int lastAttackedTicks = ((LivingEntityAccessor)mob).getLastAttackedTicks();

                    for(sweepingLevel = 0; sweepingLevel < targets.size(); ++sweepingLevel) {
                        Entity entity = targets.get(sweepingLevel);

                        if (entity != null && (!entity.equals(mob.getVehicle()) || TargetHelper.isAttackableMount(entity)) && (!(entity instanceof ArmorStand) || !((ArmorStand)entity).isMarker())) {
                            LivingEntity livingEntity;
                            if (entity instanceof LivingEntity) {
                                livingEntity = (LivingEntity)entity;
                                if (BetterCombat.config.allow_fast_attacks) {
                                    livingEntity.invulnerableTime = 0;
                                }

                                if (knockbackMultiplier != 1.0F) {
                                    ((ConfigurableKnockback)livingEntity).setKnockbackMultiplier_BetterCombat(knockbackMultiplier);
                                }
                            }

                            ((LivingEntityAccessor)mob).setLastAttackedTicks(lastAttackedTicks);
                            if (entity instanceof ItemEntity || entity instanceof ExperienceOrb || entity instanceof AbstractArrow || entity == mob) {
                                Constants.LOG.warn("Mob {} tried to attack an invalid entity", mob.getName().getString());
                                return;
                            }

                            mob.doHurtTarget(entity);

                            if (entity instanceof LivingEntity) {
                                livingEntity = (LivingEntity)entity;
                                if (knockbackMultiplier != 1.0F) {
                                    ((ConfigurableKnockback)livingEntity).setKnockbackMultiplier_BetterCombat(1.0F);
                                }
                            }
                        }
                    }

                    mob.setNoActionTime(0);

                    if (comboAttributes != null) {
                        mob.getAttributes().removeAttributeModifiers(comboAttributes);
                        if (hand.isOffHand()) {
                            setAttributesForOffHandAttack(mob, false);
                        }
                    }

                    if (dualWieldingAttributes != null) {
                        mob.getAttributes().removeAttributeModifiers(dualWieldingAttributes);
                    }

                    if (!sweepingModifiers.isEmpty()) {
                        mob.getAttributes().removeAttributeModifiers(sweepingModifiers);
                    }

                    ((PlayerAttackProperties)mob).setComboCount(-1);
                });
            }
        }
    }

    public static void sweepAttack(Mob mob) {
        double xOffset = -Mth.sin(mob.getYRot() * Mth.DEG_TO_RAD);
        double zOffset = Mth.cos(mob.getYRot() * Mth.DEG_TO_RAD);
        if (mob.level() instanceof ServerLevel) {
            ((ServerLevel)mob.level()).sendParticles(ParticleTypes.SWEEP_ATTACK, mob.getX() + xOffset, mob.getY(0.5D), mob.getZ() + zOffset, 0, xOffset, 0.0D, zOffset, 0.0D);
        }

    }

    private record AttackSelection(WeaponAttributes.Attack attack, ComboState comboState) {
        private AttackSelection(WeaponAttributes.Attack attack, ComboState comboState) {
            this.attack = attack;
            this.comboState = comboState;
        }

        public WeaponAttributes.Attack attack() {
            return this.attack;
        }

        public ComboState comboState() {
            return this.comboState;
        }
    }
}