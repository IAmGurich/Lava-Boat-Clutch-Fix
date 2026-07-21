package com.lavaboatclutch.config;

import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * NeoForge configuration (TOML, rendered by NeoForge's built-in configuration screen).
 * <p>
 * All values live at the root of the spec, so the config screen opens straight into the settings
 * instead of making the player click through a section first.
 * <p>
 * Values are baked into plain static fields, so the mixins never touch NightConfig on the hot path.
 */
public final class LavaBoatClutchConfig {

    public enum DropBounceMode {
        DEFAULT,
        CUSTOM,
        RANDOM
    }

    public static final String KEY_ENABLE_MOD     = "enableMod";
    public static final String KEY_IMMUNITY_TICKS = "immunityTicks";
    public static final String KEY_BOUNCE_MODE    = "dropBounceMode";
    public static final String KEY_BOUNCE_Y       = "bounceDropY";
    public static final String KEY_BOUNCE_X       = "bounceDropX";
    public static final String KEY_BOUNCE_Z       = "bounceDropZ";

    public static final int    MIN_IMMUNITY_TICKS     = 1;
    public static final int    MAX_IMMUNITY_TICKS     = 20;
    public static final int    DEFAULT_IMMUNITY_TICKS = 3;

    public static final float  VANILLA_BOUNCE_DROP    = 0.15F;

    public static final double MIN_BOUNCE_DROP        = 0.0D;
    public static final double MAX_BOUNCE_DROP        = 0.5D;
    public static final double MIN_BOUNCE_HORIZ       = -0.5D;
    public static final double MAX_BOUNCE_HORIZ       = 0.5D;

    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue              ENABLE_MOD;
    private static final ModConfigSpec.IntValue                  IMMUNITY_TICKS;
    private static final ModConfigSpec.EnumValue<DropBounceMode> BOUNCE_MODE;
    private static final ModConfigSpec.DoubleValue               BOUNCE_Y;
    private static final ModConfigSpec.DoubleValue               BOUNCE_X;
    private static final ModConfigSpec.DoubleValue               BOUNCE_Z;

    public static boolean        enableMod         = true;
    public static int            lavaImmunityTicks = DEFAULT_IMMUNITY_TICKS;
    public static DropBounceMode dropBounceMode    = DropBounceMode.DEFAULT;
    public static float          bounceDrop        = 0.0F;
    public static float          bounceDropX       = 0.0F;
    public static float          bounceDropZ       = 0.0F;

    static {
        final ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

        ENABLE_MOD = builder
                .comment("Enables or disables the lava boat clutch mechanic.")
                .translation("config.lava_boat_clutch.enable_mod")
                .define(KEY_ENABLE_MOD, true);

        IMMUNITY_TICKS = builder
                .comment("The number of ticks in which the boat does not burn in the lava.")
                .translation("config.lava_boat_clutch.immunity_ticks")
                .defineInRange(KEY_IMMUNITY_TICKS, DEFAULT_IMMUNITY_TICKS,
                               MIN_IMMUNITY_TICKS, MAX_IMMUNITY_TICKS);

        BOUNCE_MODE = builder
                .comment("DEFAULT: uses vanilla velocity.",
                         "CUSTOM: uses configurable velocity.",
                         "RANDOM: uses random velocity.")
                .translation("config.lava_boat_clutch.bounce_drop_mode")
                .defineEnum(KEY_BOUNCE_MODE, DropBounceMode.DEFAULT);

        BOUNCE_Y = builder
                .comment("Upward velocity applied to the boat's drop after boat burn in lava.",
                         "Only used when Drop Bounce Mode is CUSTOM.")
                .translation("config.lava_boat_clutch.bounce_drop_y")
                .defineInRange(KEY_BOUNCE_Y, 0.0D, MIN_BOUNCE_DROP, MAX_BOUNCE_DROP);

        BOUNCE_X = builder
                .comment("Lateral velocity along the X axis applied to the boat's drop after boat burn in lava.",
                         "Only used when Drop Bounce Mode is CUSTOM.")
                .translation("config.lava_boat_clutch.bounce_drop_x")
                .defineInRange(KEY_BOUNCE_X, 0.0D, MIN_BOUNCE_HORIZ, MAX_BOUNCE_HORIZ);

        BOUNCE_Z = builder
                .comment("Lateral velocity along the Z axis applied to the boat's drop after boat burn in lava.",
                         "Only used when Drop Bounce Mode is CUSTOM.")
                .translation("config.lava_boat_clutch.bounce_drop_z")
                .defineInRange(KEY_BOUNCE_Z, 0.0D, MIN_BOUNCE_HORIZ, MAX_BOUNCE_HORIZ);

        SPEC = builder.build();
    }

    private LavaBoatClutchConfig() {}

    /** True for the three velocity keys, which are only meaningful in CUSTOM mode. */
    public static boolean isVelocityKey(final String key) {
        return KEY_BOUNCE_Y.equals(key) || KEY_BOUNCE_X.equals(key) || KEY_BOUNCE_Z.equals(key);
    }

    public static void onLoad(final ModConfigEvent.Loading event) {
        bake(event.getConfig());
    }

    public static void onReload(final ModConfigEvent.Reloading event) {
        bake(event.getConfig());
    }

    private static void bake(final ModConfig config) {
        if (config.getSpec() != SPEC) {
            return;
        }

        enableMod         = ENABLE_MOD.get();
        lavaImmunityTicks = IMMUNITY_TICKS.get();
        dropBounceMode    = BOUNCE_MODE.get();
        bounceDrop        = BOUNCE_Y.get().floatValue();
        bounceDropX       = BOUNCE_X.get().floatValue();
        bounceDropZ       = BOUNCE_Z.get().floatValue();
    }
}
