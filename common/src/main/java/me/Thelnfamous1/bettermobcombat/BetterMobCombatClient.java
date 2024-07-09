package me.Thelnfamous1.bettermobcombat;

import net.bettercombat.api.client.BetterCombatClientEvents;
import net.bettercombat.client.BetterCombatClient;
import net.bettercombat.config.ClientConfig;
import net.bettercombat.logic.PlayerAttackHelper;

public class BetterMobCombatClient {

    public static void init(){
        BetterCombatClientEvents.ATTACK_START.register((player, attackHand) -> BetterMobCombat.debugTriggeredAttack(player, attackHand, PlayerAttackHelper::getAttackCooldownTicksCapped));
    }

    public static ClientConfig getBetterCombatClientConfig(){
        return BetterCombatClient.config;
    }
}
