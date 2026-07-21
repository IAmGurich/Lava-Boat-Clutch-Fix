package com.lavaboatclutch;

import com.lavaboatclutch.config.LavaBoatClutchConfig;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LavaBoatClutchMod.MOD_ID)
public class LavaBoatClutchMod {

    public static final String MOD_ID = "lava_boat_clutch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public LavaBoatClutchMod(IEventBus modBus, ModContainer container) {
        container.registerConfig(ModConfig.Type.COMMON, LavaBoatClutchConfig.SPEC);

        modBus.addListener(LavaBoatClutchConfig::onLoad);
        modBus.addListener(LavaBoatClutchConfig::onReload);

        LOGGER.info("[LavaBoatClutch] Loaded (NeoForge)!");
    }
}
