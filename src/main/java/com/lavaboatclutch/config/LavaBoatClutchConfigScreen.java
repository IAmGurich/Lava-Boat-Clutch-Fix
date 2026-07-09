package com.lavaboatclutch.config;

import com.lavaboatclutch.LavaBoatClutchMod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.function.Consumer;

public class LavaBoatClutchConfigScreen {

    private static int   toSlider(float f) { return Math.round(f * 100f); }
    private static float fromSlider(int i)  { return i / 100f; }

    private static final int SLIDER_Y_MIN  = toSlider(LavaBoatClutchConfig.MIN_BOUNCE_DROP);
    private static final int SLIDER_Y_MAX  = toSlider(LavaBoatClutchConfig.MAX_BOUNCE_DROP);
    private static final int SLIDER_XZ_MIN = toSlider(LavaBoatClutchConfig.MIN_BOUNCE_HORIZ);
    private static final int SLIDER_XZ_MAX = toSlider(LavaBoatClutchConfig.MAX_BOUNCE_HORIZ);

    public static Screen create(Screen parent) {
        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.translatable("config.lava_boat_clutch.title"))
                .setSavingRunnable(() -> {
                    cfg.clamp();
                    cfg.save();
                    LavaBoatClutchMod.LOGGER.debug("[LavaBoatClutch] Config saved.");
                });

        ConfigEntryBuilder entry = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                Text.translatable("config.lava_boat_clutch.category.general"));

        general.addEntry(entry
                .startBooleanToggle(
                        Text.translatable("config.lava_boat_clutch.enable_mod"),
                        cfg.enableMod)
                .setDefaultValue(true)
                .setTooltip(Text.translatable("config.lava_boat_clutch.enable_mod.tooltip"))
                .setSaveConsumer(val -> cfg.enableMod = val)
                .build());

        general.addEntry(entry
                .startIntSlider(
                        Text.translatable("config.lava_boat_clutch.immunity_ticks"),
                        cfg.lavaImmunityTicks,
                        LavaBoatClutchConfig.MIN_IMMUNITY_TICKS,
                        LavaBoatClutchConfig.MAX_IMMUNITY_TICKS)
                .setDefaultValue(LavaBoatClutchConfig.DEFAULT_IMMUNITY_TICKS)
                .setTooltip(Text.translatable("config.lava_boat_clutch.immunity_ticks.tooltip"))
                .setSaveConsumer(val -> cfg.lavaImmunityTicks = val)
                .build());

        var modeEntry = entry
                .startSelector(
                        Text.translatable("config.lava_boat_clutch.bounce_drop_mode"),
                        LavaBoatClutchConfig.BounceDropMode.values(),
                        cfg.bounceDropMode)
                .setDefaultValue(LavaBoatClutchConfig.BounceDropMode.DEFAULT)
                .setNameProvider(mode -> Text.translatable(
                        "config.lava_boat_clutch.bounce_drop_mode." + mode.name().toLowerCase()))
                .setTooltip(Text.translatable("config.lava_boat_clutch.bounce_drop_mode.tooltip"))
                .setSaveConsumer(val -> cfg.bounceDropMode = val)
                .build();

        general.addEntry(modeEntry);

        Requirement customModeActive = Requirement.isTrue(
                () -> modeEntry.getValue() == LavaBoatClutchConfig.BounceDropMode.CUSTOM);

        addVelocitySlider(general, entry, "config.lava_boat_clutch.bounce_drop_y",
                cfg.bounceDrop, SLIDER_Y_MIN, SLIDER_Y_MAX,
                LavaBoatClutchConfig.DEFAULT_BOUNCE_DROP, "%.2f",
                val -> cfg.bounceDrop = fromSlider(val), customModeActive);

        addVelocitySlider(general, entry, "config.lava_boat_clutch.bounce_drop_x",
                cfg.bounceDropX, SLIDER_XZ_MIN, SLIDER_XZ_MAX,
                LavaBoatClutchConfig.DEFAULT_BOUNCE_HORIZ, "%+.2f",
                val -> cfg.bounceDropX = fromSlider(val), customModeActive);

        addVelocitySlider(general, entry, "config.lava_boat_clutch.bounce_drop_z",
                cfg.bounceDropZ, SLIDER_XZ_MIN, SLIDER_XZ_MAX,
                LavaBoatClutchConfig.DEFAULT_BOUNCE_HORIZ, "%+.2f",
                val -> cfg.bounceDropZ = fromSlider(val), customModeActive);

        return builder.build();
    }

    private static void addVelocitySlider(ConfigCategory category,
                                          ConfigEntryBuilder entry,
                                          String key,
                                          float current,
                                          int min,
                                          int max,
                                          float defaultValue,
                                          String format,
                                          Consumer<Integer> saveConsumer,
                                          Requirement requirement) {
        category.addEntry(entry
                .startIntSlider(Text.translatable(key), toSlider(current), min, max)
                .setDefaultValue(toSlider(defaultValue))
                .setTextGetter(val -> Text.literal(String.format(format, fromSlider(val))))
                .setTooltip(Text.translatable(key + ".tooltip"))
                .setSaveConsumer(saveConsumer)
                .setRequirement(requirement)
                .build());
    }
}
