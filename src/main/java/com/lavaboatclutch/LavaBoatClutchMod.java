package com.lavaboatclutch;

import com.lavaboatclutch.config.LavaBoatClutchConfig;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LavaBoatClutchMod implements ModInitializer {

    public static final String MOD_ID = "lava_boat_clutch";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static LavaBoatClutchConfig config;

    @Override
    public void onInitialize() {
        config = LavaBoatClutchConfig.load();
        LOGGER.info(
            "[LavaBoatClutch] Loaded! enabled={}, immunityTicks={}, " +
            "bounceMode={}, bounceY={}, bounceX={}, bounceZ={}",
            config.enableMod,
            config.lavaImmunityTicks,
            config.bounceDropMode,
            config.getEffectiveBounce(),
            config.isCustomMode() ? config.bounceDropX : "n/a",
            config.isCustomMode() ? config.bounceDropZ : "n/a");
    }

    public static LavaBoatClutchConfig getConfig() {
        return config;
    }
}
