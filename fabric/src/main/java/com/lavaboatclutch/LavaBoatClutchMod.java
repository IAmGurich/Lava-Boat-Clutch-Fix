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

        Object bounceY;
        Object bounceX = "n/a";
        Object bounceZ = "n/a";
        switch (config.dropBounceMode) {
            case CUSTOM -> {
                bounceY = config.bounceDrop;
                bounceX = config.bounceDropX;
                bounceZ = config.bounceDropZ;
            }
            case RANDOM -> bounceY = "random";
            default     -> bounceY = LavaBoatClutchConfig.VANILLA_BOUNCE_DROP;
        }

        LOGGER.info(
            "[LavaBoatClutch] Loaded! enabled={}, immunityTicks={}, " +
            "bounceMode={}, bounceY={}, bounceX={}, bounceZ={}",
            config.enableMod, config.lavaImmunityTicks, config.dropBounceMode,
            bounceY, bounceX, bounceZ);
    }

    public static LavaBoatClutchConfig getConfig() {
        return config;
    }
}
