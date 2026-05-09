package com.lavaboatclutch.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LavaBoatClutchConfig {

    public static final int MIN_IMMUNITY_TICKS     = 1;
    public static final int MAX_IMMUNITY_TICKS     = 20;
    public static final int DEFAULT_IMMUNITY_TICKS = 3;

    public static final float VANILLA_BOUNCE_DROP = 0.15f;

    public static final float MIN_BOUNCE_DROP     = 0.0f;
    public static final float MAX_BOUNCE_DROP     = 0.5f;

    public static final float DEFAULT_BOUNCE_DROP = 0.0f;

    public static final float MIN_BOUNCE_HORIZ     = -0.50f;
    public static final float MAX_BOUNCE_HORIZ     =  0.50f;

    public static final float DEFAULT_BOUNCE_HORIZ =  0.0f;

    private static final Logger LOGGER = LoggerFactory.getLogger("lava_boat_clutch/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("lava_boat_clutch.json");

    public boolean enableMod = true;

    public int lavaImmunityTicks = DEFAULT_IMMUNITY_TICKS;

    public boolean bounceDropCustom = false;

    public float bounceDrop = DEFAULT_BOUNCE_DROP;

    public float bounceDropX = DEFAULT_BOUNCE_HORIZ;

    public float bounceDropZ = DEFAULT_BOUNCE_HORIZ;

    public float getEffectiveBounce() {
        return bounceDropCustom ? bounceDrop : VANILLA_BOUNCE_DROP;
    }

    public boolean isVanillaMode() {
        return !bounceDropCustom;
    }

    public static LavaBoatClutchConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                LavaBoatClutchConfig cfg = GSON.fromJson(json, LavaBoatClutchConfig.class);
                if (cfg != null) {
                    cfg.clamp();
                    return cfg;
                }
                LOGGER.warn("[LavaBoatClutch] Config deserialized to null — falling back to defaults.");
            } catch (IOException e) {
                LOGGER.warn("[LavaBoatClutch] Failed to read config file: {} — falling back to defaults.", e.getMessage());
            } catch (com.google.gson.JsonParseException e) {
                LOGGER.warn("[LavaBoatClutch] Config file contains invalid JSON: {} — falling back to defaults.", e.getMessage());
            }
        }
        LavaBoatClutchConfig defaults = new LavaBoatClutchConfig();
        defaults.save();
        return defaults;
    }

    public void save() {
        try {

            Files.createDirectories(CONFIG_PATH.getParent());
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException e) {
            LOGGER.warn("[LavaBoatClutch] Failed to save config: {}", e.getMessage());
        }
    }

    public void clamp() {
        lavaImmunityTicks = Math.max(MIN_IMMUNITY_TICKS,
                            Math.min(MAX_IMMUNITY_TICKS, lavaImmunityTicks));
        bounceDrop  = Math.max(MIN_BOUNCE_DROP,   Math.min(MAX_BOUNCE_DROP,   bounceDrop));
        bounceDropX = Math.max(MIN_BOUNCE_HORIZ,  Math.min(MAX_BOUNCE_HORIZ,  bounceDropX));
        bounceDropZ = Math.max(MIN_BOUNCE_HORIZ,  Math.min(MAX_BOUNCE_HORIZ,  bounceDropZ));
    }
}
