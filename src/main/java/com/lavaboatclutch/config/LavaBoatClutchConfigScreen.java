package com.lavaboatclutch.config;

import com.lavaboatclutch.LavaBoatClutchMod;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.api.Requirement;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

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
                .setTitle(Component.translatable("config.lava_boat_clutch.title"))
                .setSavingRunnable(() -> {
                    cfg.clamp();
                    cfg.save();
                    LavaBoatClutchMod.LOGGER.debug("[LavaBoatClutch] Config saved.");
                });

        ConfigEntryBuilder entry = builder.entryBuilder();

        ConfigCategory general = builder.getOrCreateCategory(
                Component.translatable("config.lava_boat_clutch.category.general"));

        general.addEntry(entry
                .startBooleanToggle(
                        Component.translatable("config.lava_boat_clutch.enable_mod"),
                        cfg.enableMod)
                .setDefaultValue(true)
                .setTooltip(
                        Component.translatable("config.lava_boat_clutch.enable_mod.tooltip"),
                        Component.translatable("config.lava_boat_clutch.enable_mod.tooltip.2"))
                .setSaveConsumer(val -> cfg.enableMod = val)
                .build());

        general.addEntry(entry
                .startIntSlider(
                        Component.translatable("config.lava_boat_clutch.immunity_ticks"),
                        cfg.lavaImmunityTicks,
                        LavaBoatClutchConfig.MIN_IMMUNITY_TICKS,
                        LavaBoatClutchConfig.MAX_IMMUNITY_TICKS)
                .setDefaultValue(LavaBoatClutchConfig.DEFAULT_IMMUNITY_TICKS)
                .setTooltip(
                        Component.translatable("config.lava_boat_clutch.immunity_ticks.tooltip"),
                        Component.translatable("config.lava_boat_clutch.immunity_ticks.tooltip.2"))
                .setSaveConsumer(val -> cfg.lavaImmunityTicks = val)
                .build());

        var modeEntry = entry
                .startBooleanToggle(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_mode"),
                        cfg.bounceDropCustom)
                .setDefaultValue(false)
                .setYesNoTextSupplier(val -> val
                        ? Component.translatable("config.lava_boat_clutch.bounce_drop_mode.custom")
                        : Component.translatable("config.lava_boat_clutch.bounce_drop_mode.default"))
                .setTooltip(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_mode.tooltip"),
                        Component.translatable("config.lava_boat_clutch.bounce_drop_mode.tooltip.2"))
                .setSaveConsumer(val -> cfg.bounceDropCustom = val)
                .build();

        general.addEntry(modeEntry);

        Requirement customModeActive = Requirement.isTrue(() -> modeEntry.getValue());

        general.addEntry(entry
                .startIntSlider(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_y"),
                        toSlider(cfg.bounceDrop),
                        SLIDER_Y_MIN,
                        SLIDER_Y_MAX)
                .setDefaultValue(toSlider(LavaBoatClutchConfig.DEFAULT_BOUNCE_DROP))
                .setTextGetter(val -> Component.literal(String.format("%.2f", fromSlider(val))))
                .setTooltip(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_y.tooltip"),
                        Component.translatable("config.lava_boat_clutch.bounce_drop_y.tooltip.2"))
                .setSaveConsumer(val -> cfg.bounceDrop = fromSlider(val))
                .setRequirement(customModeActive)
                .build());

        general.addEntry(entry
                .startIntSlider(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_x"),
                        toSlider(cfg.bounceDropX),
                        SLIDER_XZ_MIN,
                        SLIDER_XZ_MAX)
                .setDefaultValue(toSlider(LavaBoatClutchConfig.DEFAULT_BOUNCE_HORIZ))
                .setTextGetter(val -> Component.literal(String.format("%+.2f", fromSlider(val))))
                .setTooltip(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_x.tooltip"),
                        Component.translatable("config.lava_boat_clutch.bounce_drop_x.tooltip.2"))
                .setSaveConsumer(val -> cfg.bounceDropX = fromSlider(val))
                .setRequirement(customModeActive)
                .build());

        general.addEntry(entry
                .startIntSlider(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_z"),
                        toSlider(cfg.bounceDropZ),
                        SLIDER_XZ_MIN,
                        SLIDER_XZ_MAX)
                .setDefaultValue(toSlider(LavaBoatClutchConfig.DEFAULT_BOUNCE_HORIZ))
                .setTextGetter(val -> Component.literal(String.format("%+.2f", fromSlider(val))))
                .setTooltip(
                        Component.translatable("config.lava_boat_clutch.bounce_drop_z.tooltip"),
                        Component.translatable("config.lava_boat_clutch.bounce_drop_z.tooltip.2"))
                .setSaveConsumer(val -> cfg.bounceDropZ = fromSlider(val))
                .setRequirement(customModeActive)
                .build());

        return builder.build();
    }
}
