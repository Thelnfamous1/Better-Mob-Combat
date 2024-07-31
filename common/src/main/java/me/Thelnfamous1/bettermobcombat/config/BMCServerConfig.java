package me.Thelnfamous1.bettermobcombat.config;

import com.google.gson.Gson;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.bettercombat.logic.TargetHelper;
import net.minecraft.Util;

import java.util.LinkedHashMap;

@Config(
    name = "server"
)
public class BMCServerConfig implements ConfigData {
    private static final Gson GSON = new Gson();
    @Comment("The additional attack cooldown applied to a mob, in ticks, after it launches a Better Combat attack. \nIn vanilla, there is usually a delay of 20 ticks between a mob's attacks. \n7 was chosen because a default Better Combat sword attack takes 13 ticks.")
    @ConfigEntry.BoundedDiscrete(
            max = 100L
    )
    public int mob_additional_attack_cooldown = 7;
    @Comment("Controls which mobs are blacklisted from using the Better Combat system. \nHelpful if certain mobs aren't able to properly animate Better Combat attacks.")
    public String[] mob_blacklist = new String[]{"minecraft:fox"};

    @Comment("Allows the mob blacklist to instead be used as a whitelist. \nThis makes it so that only mobs in the whitelist can use the Better Combat system.")
    public boolean mob_blacklist_as_whitelist = false;
    @Comment("Automatically blacklists mobs detected to be using a GeckoLib model from using the Better Combat system. \nGeckoLib models are not supported by Mob Player Animator, and therefore won't animate Better Combat attacks.")
    public boolean geckolib_mobs_blacklisted = true;
    @Comment("Allows mobs to perform the vanilla check for allies. This allows for certain mobs, such as Illagers, to recognize each other as natural allies if neither they nor their target are assigned to scoreboard teams. \nIf this check fails, the system will fallback to a scoreboard team ally check, followed by the specified Better Combat target relation checks.")
    public boolean mobs_check_for_vanilla_allies = true;
    @Comment("Determines if mobs that are assigned to scoreboard teams will  only respect scoreboard team ally checks and ignore any Better Combat target relation checks.")
    public boolean team_mobs_only_respect_teams = false;
    @Comment("Relations determine when mobs' undirected weapon swings (cleaves) will hurt another entity (target).\n- `FRIENDLY` - The target can never be damaged by the mob.\n- `NEUTRAL` - The target can be damaged only if the mob is directly targeting it.\n- `HOSTILE` - The target can be damaged if located within the weapon swing area.\n(NOTE: Vanilla sweeping can still hit targets, if not disabled via `allow_sweeping`)\n\nThe various relation related configs are being checked in the following order:\n- `mob_relations`\n- `mob_relations_to_passives`\n- `mob_relations_to_hostiles`\n- `mob_relations_to_other`\n(The first relation to be found for the target will be applied. If no relation is found, it will default to HOSTILE.)\n")
    public LinkedHashMap<String, LinkedHashMap<String, TargetHelper.Relation>> mob_relations = new LinkedHashMap<String, LinkedHashMap<String, TargetHelper.Relation>>() {
        {
            this.put("guardvillagers:guard", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultVillagerAllyRelations));
            this.put("recruits:recruit", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultVillagerAllyRelations));
            this.put("recruits:bowman", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultVillagerAllyRelations));
            this.put("recruits:recruit_shieldman", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultVillagerAllyRelations));
            this.put("recruits:nomad", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultVillagerAllyRelations));
            this.put("recruits:horseman", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultVillagerAllyRelations));
            this.put("minecraft:piglin", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultPiglinRelations));
            this.put("minecraft:piglin_brute", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultPiglinRelations));
            this.put("minecraft:evoker", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultIllagerRelations));
            this.put("minecraft:illusioner", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultIllagerRelations));
            this.put("minecraft:pillager", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultIllagerRelations));
            this.put("minecraft:vindicator", Util.make(new LinkedHashMap<>(), BMCServerConfig::addDefaultIllagerRelations));
            this.put("minecraft:zombified_piglin", Util.make(new LinkedHashMap<>(), map -> map.put("minecraft:zombified_piglin", TargetHelper.Relation.NEUTRAL)));
        }
    };

    private static void addDefaultIllagerRelations(LinkedHashMap<String, TargetHelper.Relation> map) {
        map.put("minecraft:evoker", TargetHelper.Relation.NEUTRAL);
        map.put("minecraft:illusioner", TargetHelper.Relation.NEUTRAL);
        map.put("minecraft:pillager", TargetHelper.Relation.NEUTRAL);
        map.put("minecraft:vindicator", TargetHelper.Relation.NEUTRAL);
    }

    private static void addDefaultVillagerAllyRelations(LinkedHashMap<String, TargetHelper.Relation> map) {
        map.put("guardvillagers:guard", TargetHelper.Relation.NEUTRAL);
        map.put("minecraft:villager", TargetHelper.Relation.NEUTRAL);
        map.put("minecraft:iron_golem", TargetHelper.Relation.NEUTRAL);
        map.put("recruits:recruit", TargetHelper.Relation.NEUTRAL);
        map.put("recruits:bowman", TargetHelper.Relation.NEUTRAL);
        map.put("recruits:recruit_shieldman", TargetHelper.Relation.NEUTRAL);
        map.put("recruits:nomad", TargetHelper.Relation.NEUTRAL);
        map.put("recruits:horseman", TargetHelper.Relation.NEUTRAL);
    }

    private static void addDefaultPiglinRelations(LinkedHashMap<String, TargetHelper.Relation> map) {
        map.put("minecraft:piglin", TargetHelper.Relation.NEUTRAL);
        map.put("minecraft:piglin_brute", TargetHelper.Relation.NEUTRAL);
    }

    @Comment("Relation to unspecified entities that are instances of PassiveEntity(Yarn)/AgeableEntity(Mojmap)")
    public LinkedHashMap<String, TargetHelper.Relation> mob_relations_to_passives = new LinkedHashMap<String, TargetHelper.Relation>();
    @Comment("Relation to unspecified entities that are instances of HostileEntity(Yarn)/MonsterEntity(Mojmap)")
    public LinkedHashMap<String, TargetHelper.Relation> mob_relations_to_hostiles = new LinkedHashMap<String, TargetHelper.Relation>();
    @Comment("Fallback relation")
    public LinkedHashMap<String, TargetHelper.Relation> mob_relations_to_other = new LinkedHashMap<String, TargetHelper.Relation>();

    public BMCServerConfig() {
    }

    public static BMCServerConfig deserialize(String json) {
        return GSON.fromJson(json, BMCServerConfig.class);
    }

    public String serialize() {
        return GSON.toJson(this);
    }

}