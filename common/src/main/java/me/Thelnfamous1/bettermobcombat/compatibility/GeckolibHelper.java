package me.Thelnfamous1.bettermobcombat.compatibility;

import net.minecraft.world.entity.Entity;
import software.bernie.geckolib.core.animatable.GeoAnimatable;

public class GeckolibHelper {

    public static boolean isGeoAnimatable(Entity entity){
        return entity instanceof GeoAnimatable;
    }
}
