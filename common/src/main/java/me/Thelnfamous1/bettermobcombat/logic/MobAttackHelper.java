package me.Thelnfamous1.bettermobcombat.logic;

import me.Thelnfamous1.bettermobcombat.api.MobAttackStrength;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
import net.bettercombat.api.ComboState;
import net.bettercombat.api.WeaponAttributes;
import net.bettercombat.logic.WeaponRegistry;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;

import java.util.Arrays;

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