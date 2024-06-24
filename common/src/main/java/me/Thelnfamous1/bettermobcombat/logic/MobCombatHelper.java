package me.Thelnfamous1.bettermobcombat.logic;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.mixin.MobAccessor;
import me.Thelnfamous1.bettermobcombat.mixin.ServerNetworkAccessor;
import net.bettercombat.BetterCombat;
import net.bettercombat.api.AttackHand;
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
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.List;

public class MobCombatHelper {
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
            AttackHand hand = MobAttackHelper.getCurrentAttack(mob, comboCount);
            if (hand == null) {
                Constants.LOG.error("Server handling attack for {} - No current attack hand!", mob);
                Constants.LOG.error("Combo count: " + comboCount + " is dual wielding: " + MobAttackHelper.isDualWielding(mob));
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
                        float dualWieldingMultiplier = MobAttackHelper.getDualWieldingAttackDamageMultiplier(mob, hand) - 1.0F;
                        if (dualWieldingMultiplier != 0.0F) {
                            dualWieldingAttributes = HashMultimap.create();
                            dualWieldingAttributes.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(ServerNetworkAccessor.bettermobcombat$getDUAL_WIELDING_MODIFIER_ID(), "DUAL_WIELDING_DAMAGE_MULTIPLIER", dualWieldingMultiplier, AttributeModifier.Operation.MULTIPLY_TOTAL));
                            mob.getAttributes().addTransientAttributeModifiers(dualWieldingAttributes);
                        }

                        if (hand.isOffHand()) {
                            MobAttackHelper.setAttributesForOffHandAttack(mob, true);
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

                    float attackCooldown = MobAttackHelper.getAttackCooldownTicksCapped(mob);
                    float knockbackMultiplier = BetterCombat.config.knockback_reduced_for_fast_attacks ? MathHelper.clamp(attackCooldown / 12.5F, 0.1F, 1.0F) : 1.0F;
                    int lastAttackedTicks = ((LivingEntityAccessor)mob).getLastAttackedTicks();

                    for(sweepingLevel = 0; sweepingLevel < targets.size(); ++sweepingLevel) {
                        Entity target = targets.get(sweepingLevel);

                        if (target != null && (!target.equals(mob.getVehicle()) || TargetHelper.isAttackableMount(target)) && (!(target instanceof ArmorStand) || !((ArmorStand)target).isMarker())) {
                            LivingEntity livingTarget = target instanceof LivingEntity ? (LivingEntity) target : null;
                            if (livingTarget != null) {
                                if (BetterCombat.config.allow_fast_attacks) {
                                    livingTarget.invulnerableTime = 0;
                                }

                                if (knockbackMultiplier != 1.0F) {
                                    ((ConfigurableKnockback)livingTarget).setKnockbackMultiplier_BetterCombat(knockbackMultiplier);
                                }
                            }

                            ((LivingEntityAccessor)mob).setLastAttackedTicks(lastAttackedTicks);
                            if (target instanceof ItemEntity || target instanceof ExperienceOrb || target instanceof AbstractArrow || target == mob) {
                                Constants.LOG.error("{} tried to attack an invalid entity - {}", mob.getName().getString(), target);
                                return;
                            }

                            mob.doHurtTarget(target);

                            if (livingTarget != null) {
                                if (knockbackMultiplier != 1.0F) {
                                    ((ConfigurableKnockback)livingTarget).setKnockbackMultiplier_BetterCombat(1.0F);
                                }
                            }
                        }
                    }

                    mob.setNoActionTime(0);

                    if (comboAttributes != null) {
                        mob.getAttributes().removeAttributeModifiers(comboAttributes);
                        if (hand.isOffHand()) {
                            MobAttackHelper.setAttributesForOffHandAttack(mob, false);
                        }
                    }

                    if (dualWieldingAttributes != null) {
                        mob.getAttributes().removeAttributeModifiers(dualWieldingAttributes);
                    }

                    if (!sweepingModifiers.isEmpty()) {
                        mob.getAttributes().removeAttributeModifiers(sweepingModifiers);
                    }

                    //((PlayerAttackProperties)mob).setComboCount(-1);
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

    public static double calculateAttributeValue(Attribute attribute, double baseValue, Collection<AttributeModifier> modifiers) {
        double sumValue = baseValue;

        for(AttributeModifier additive : modifiers.stream().filter(mod -> mod.getOperation().equals(AttributeModifier.Operation.ADDITION)).toList()) {
            sumValue += additive.getAmount();
        }

        double productValue = sumValue;

        for(AttributeModifier baseMultiplicative : modifiers.stream().filter(mod -> mod.getOperation().equals(AttributeModifier.Operation.MULTIPLY_BASE)).toList()) {
            productValue += sumValue * baseMultiplicative.getAmount();
        }

        for(AttributeModifier totalMultiplicative : modifiers.stream().filter(mod -> mod.getOperation().equals(AttributeModifier.Operation.MULTIPLY_TOTAL)).toList()) {
            productValue *= 1.0D + totalMultiplicative.getAmount();
        }

        return attribute.sanitizeValue(productValue);
    }
}
