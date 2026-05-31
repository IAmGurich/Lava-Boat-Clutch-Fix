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

    // ── Drop Bounce Mode ───────────────────────────────────────────────────────
    public enum DropBounceMode {
        /** Mimics vanilla behaviour: Y=0.15, X/Z small random [-0.1, 0.1]. */
        DEFAULT,
        /** User-defined fixed velocities for X, Y, Z. */
        CUSTOM,
        /** Fully random velocities on every drop across the full configured range. */
        RANDOM
    }

    // ── Immunity ticks ─────────────────────────────────────────────────────────
    public static final int MIN_IMMUNITY_TICKS     = 1;
    public static final int MAX_IMMUNITY_TICKS     = 20;
    public static final int DEFAULT_IMMUNITY_TICKS = 3;

    // ── Bounce Y constants ─────────────────────────────────────────────────────
    public static final float VANILLA_BOUNCE_DROP  = 0.15f;

    public static final float MIN_BOUNCE_DROP      = 0.0f;
    public static final float MAX_BOUNCE_DROP      = 0.5f;
    public static final float DEFAULT_BOUNCE_DROP  = 0.0f;

    // ── Bounce X / Z constants ─────────────────────────────────────────────────
    public static final float MIN_BOUNCE_HORIZ     = -0.50f;
    public static final float MAX_BOUNCE_HORIZ     =  0.50f;
    public static final float DEFAULT_BOUNCE_HORIZ =  0.0f;

    // ── Infrastructure ─────────────────────────────────────────────────────────
    private static final Logger LOGGER = LoggerFactory.getLogger("lava_boat_clutch/config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("lava_boat_clutch.json");

    // ── Fields ─────────────────────────────────────────────────────────────────
    public boolean enableMod = true;

    public int lavaImmunityTicks = DEFAULT_IMMUNITY_TICKS;

    /**
     * Drop bounce mode: DEFAULT, CUSTOM, or RANDOM.
     *
     * <p><b>Migration note:</b> configs saved by versions ≤ 3.1.2 stored a
     * {@code bounceDropCustom} boolean instead of this field. When GSON
     * deserialises such a file, this field will be {@code null} and
     * {@link #bounceDropCustom} will carry the old value.  {@link #clamp()}
     * detects this and converts automatically.</p>
     */
    public DropBounceMode dropBounceMode = DropBounceMode.DEFAULT;

    /**
     * Legacy field kept for backward-compat deserialization only.
     * Do NOT use this field in new code — read {@link #dropBounceMode}.
     * @deprecated Replaced by {@link #dropBounceMode} in 3.2.0.
     */
    @Deprecated
    public Boolean bounceDropCustom = null;

    /** Custom Y velocity (upward kick applied to the boat drop; CUSTOM mode only). */
    public float bounceDrop = DEFAULT_BOUNCE_DROP;

    /** Custom X velocity (lateral drift; CUSTOM mode only). */
    public float bounceDropX = DEFAULT_BOUNCE_HORIZ;

    /** Custom Z velocity (lateral drift; CUSTOM mode only). */
    public float bounceDropZ = DEFAULT_BOUNCE_HORIZ;

    // ── Helpers ────────────────────────────────────────────────────────────────

    /**
     * Returns the fixed Y bounce velocity:
     * <ul>
     *   <li>CUSTOM → {@link #bounceDrop} (user value)</li>
     *   <li>DEFAULT / RANDOM → {@link #VANILLA_BOUNCE_DROP}</li>
     * </ul>
     */
    public float getEffectiveBounce() {
        return dropBounceMode == DropBounceMode.CUSTOM ? bounceDrop : VANILLA_BOUNCE_DROP;
    }

    public boolean isDefaultMode() { return dropBounceMode == DropBounceMode.DEFAULT; }
    public boolean isCustomMode()  { return dropBounceMode == DropBounceMode.CUSTOM; }
    public boolean isRandomMode()  { return dropBounceMode == DropBounceMode.RANDOM; }

    // ── I/O ────────────────────────────────────────────────────────────────────

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

    /**
     * Clamps all values to their valid ranges, and migrates legacy configs.
     *
     * <p>Legacy migration (≤ 3.1.2): if {@code dropBounceMode} is null (field was
     * absent in the saved JSON), the old {@code bounceDropCustom} boolean is read:
     * {@code true} → CUSTOM, {@code false/null} → DEFAULT.
     * The legacy field is then cleared so the next {@link #save()} writes a clean file.</p>
     */
    public void clamp() {
        // ── Legacy migration ───────────────────────────────────────────────────
        if (dropBounceMode == null) {
            if (Boolean.TRUE.equals(bounceDropCustom)) {
                dropBounceMode = DropBounceMode.CUSTOM;
                LOGGER.info("[LavaBoatClutch] Migrated legacy config: bounceDropCustom=true → CUSTOM");
            } else {
                dropBounceMode = DropBounceMode.DEFAULT;
            }
        }
        bounceDropCustom = null; // discard after migration

        // ── Range clamps ───────────────────────────────────────────────────────
        lavaImmunityTicks = Math.max(MIN_IMMUNITY_TICKS,
                            Math.min(MAX_IMMUNITY_TICKS, lavaImmunityTicks));
        bounceDrop  = Math.max(MIN_BOUNCE_DROP,  Math.min(MAX_BOUNCE_DROP,  bounceDrop));
        bounceDropX = Math.max(MIN_BOUNCE_HORIZ, Math.min(MAX_BOUNCE_HORIZ, bounceDropX));
        bounceDropZ = Math.max(MIN_BOUNCE_HORIZ, Math.min(MAX_BOUNCE_HORIZ, bounceDropZ));
    }
}
