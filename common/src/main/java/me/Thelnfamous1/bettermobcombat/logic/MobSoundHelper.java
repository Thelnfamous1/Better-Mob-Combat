package me.Thelnfamous1.bettermobcombat.logic;

import me.Thelnfamous1.bettermobcombat.Constants;
import me.Thelnfamous1.bettermobcombat.platform.Services;
import net.bettercombat.api.WeaponAttributes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;

import java.util.Random;

public class MobSoundHelper {
    public static final Random RNG = new Random();

    public static void playSound(ServerLevel world, Entity entity, WeaponAttributes.Sound sound) {
        if (sound != null) {
            try {
                float pitch = sound.randomness() > 0.0F ? RNG.nextFloat(sound.pitch() - sound.randomness(), sound.pitch() + sound.randomness()) : sound.pitch();
                SoundEvent soundEvent = BuiltInRegistries.SOUND_EVENT.get(new ResourceLocation(sound.id()));
                float distance = soundEvent.getRange(sound.volume());
                Services.PLATFORM.playMobAttackSound(world, entity.getId(), entity.getX(), entity.getY(), entity.getZ(), sound.id(), sound.volume(), pitch, RNG.nextLong(), distance, entity.level().dimension());
            } catch (Exception var8) {
                Constants.LOG.error("Failed to play sound: " + sound.id());
                var8.printStackTrace();
            }

        }
    }
}
