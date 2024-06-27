package me.Thelnfamous1.bettermobcombat.config;

import me.Thelnfamous1.bettermobcombat.Constants;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@Config(
    name = Constants.MOD_ID
)
public class BMCServerConfigWrapper extends PartitioningSerializer.GlobalData {
    @ConfigEntry.Category("server")
    @ConfigEntry.Gui.Excluded
    public BMCServerConfig server = new BMCServerConfig();

    public BMCServerConfigWrapper() {
    }
}