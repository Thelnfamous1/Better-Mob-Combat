package me.Thelnfamous1.bettermobcombat.mixin;

import net.bettercombat.network.ServerNetwork;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.UUID;

@Mixin(value = ServerNetwork.class, remap = false)
public interface ServerNetworkAccessor {

    @Accessor(value = "COMBO_DAMAGE_MODIFIER_ID", remap = false)
    static UUID bettermobcombat$getCOMBO_DAMAGE_MODIFIER_ID(){
        throw new AssertionError();
    }

    @Accessor(value = "DUAL_WIELDING_MODIFIER_ID", remap = false)
    static UUID bettermobcombat$getDUAL_WIELDING_MODIFIER_ID(){
        throw new AssertionError();
    }

    @Accessor(value = "SWEEPING_MODIFIER_ID", remap = false)
    static UUID bettermobcombat$getSWEEPING_MODIFIER_ID(){
        throw new AssertionError();
    }
}
