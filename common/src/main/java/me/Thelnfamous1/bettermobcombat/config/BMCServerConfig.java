package me.Thelnfamous1.bettermobcombat.config;

import com.google.gson.Gson;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(
    name = "server"
)
public class BMCServerConfig implements ConfigData {
    public static final Gson GSON = new Gson();
    @Comment("The additional attack cooldown applied to a mob, in ticks, after it launches a Better Combat attack. \nIn vanilla, there is usually a delay of 20 ticks between a mob's attacks. \n7 was chosen because a default Better Combat sword attack takes 13 ticks.")
    @ConfigEntry.BoundedDiscrete(
            max = 100L
    )
    public int mob_additional_attack_cooldown = 7;

    public BMCServerConfig() {
    }

    public static BMCServerConfig deserialize(String json) {
        return GSON.fromJson(json, BMCServerConfig.class);
    }

    public String serialize() {
        return GSON.toJson(this);
    }
}