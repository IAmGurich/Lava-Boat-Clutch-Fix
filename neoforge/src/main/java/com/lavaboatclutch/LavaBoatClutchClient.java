package com.lavaboatclutch;

import com.lavaboatclutch.config.LavaBoatClutchConfigScreen;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

/** Registers the configuration screen (Mods -> Lava Boat Clutch Fix -> Config). */
@Mod(value = LavaBoatClutchMod.MOD_ID, dist = Dist.CLIENT)
public class LavaBoatClutchClient {

    public LavaBoatClutchClient(final ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class,
                (mod, parent) -> new ConfigurationScreen(mod, parent, LavaBoatClutchConfigScreen::new));
    }
}
