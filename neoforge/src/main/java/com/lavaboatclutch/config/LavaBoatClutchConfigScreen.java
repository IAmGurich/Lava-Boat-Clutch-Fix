package com.lavaboatclutch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.jetbrains.annotations.Nullable;

/**
 * NeoForge's built-in configuration screen, extended so that the Velocity X/Y/Z fields are only
 * editable while Drop Bounce Mode is set to CUSTOM. They are greyed out otherwise, and toggle
 * live as soon as the mode is cycled.
 */
public class LavaBoatClutchConfigScreen extends ConfigurationScreen.ConfigurationSectionScreen {

    private final List<AbstractWidget> velocityWidgets = new ArrayList<>();

    @Nullable
    private Supplier<?> bounceModeSupplier;

    public LavaBoatClutchConfigScreen(final ConfigurationScreen parent,
                                      final ModConfig.Type type,
                                      final ModConfig modConfig,
                                      final Component title) {
        super(parent, type, modConfig, title);
    }

    @Override
    protected ConfigurationScreen.ConfigurationSectionScreen rebuild() {
        velocityWidgets.clear();
        bounceModeSupplier = null;
        return super.rebuild();
    }

    @Override
    protected <T extends Enum<T>> ConfigurationScreen.ConfigurationSectionScreen.Element createEnumValue(
            final String key,
            final ModConfigSpec.ValueSpec spec,
            final Supplier<T> source,
            final Consumer<T> target) {
        if (!LavaBoatClutchConfig.KEY_BOUNCE_MODE.equals(key)) {
            return super.createEnumValue(key, spec, source, target);
        }

        bounceModeSupplier = source;

        return super.createEnumValue(key, spec, source, value -> {
            target.accept(value);
            updateVelocityWidgets();
        });
    }

    @Override
    @Nullable
    protected ConfigurationScreen.ConfigurationSectionScreen.Element createDoubleValue(
            final String key,
            final ModConfigSpec.ValueSpec spec,
            final Supplier<Double> source,
            final Consumer<Double> target) {
        final ConfigurationScreen.ConfigurationSectionScreen.Element element =
                super.createDoubleValue(key, spec, source, target);

        if (element != null && LavaBoatClutchConfig.isVelocityKey(key)) {
            final AbstractWidget widget = element.widget();
            if (widget != null) {
                velocityWidgets.add(widget);
                setEnabled(widget, isCustomMode());
            }
        }

        return element;
    }

    @Override
    protected void onChanged(final String key) {
        super.onChanged(key);
        updateVelocityWidgets();
    }

    private boolean isCustomMode() {
        return bounceModeSupplier != null
                && bounceModeSupplier.get() == LavaBoatClutchConfig.DropBounceMode.CUSTOM;
    }

    private void updateVelocityWidgets() {
        final boolean enabled = isCustomMode();
        for (final AbstractWidget widget : velocityWidgets) {
            setEnabled(widget, enabled);
        }
    }

    private static void setEnabled(final AbstractWidget widget, final boolean enabled) {
        widget.active = enabled;
        if (widget instanceof EditBox box) {
            box.setEditable(enabled);
        }
    }
}
