package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Suppresses the fire overlay rendered on-screen when the player rides a boat
 * that has just entered lava but is still within the immunity window.
 *
 * <p>Without this mixin the flame overlay flashes briefly every time the boat
 * touches lava, even though no actual fire damage is dealt.</p>
 */
@Mixin(Entity.class)
public abstract class EntityFireMixin {

    @Inject(method = "doesRenderOnFire()Z", at = @At("HEAD"), cancellable = true)
    private void lbc_suppressFireRender(CallbackInfoReturnable<Boolean> cir) {
        // Only applies to boat entities that implement our immunity interface
        if (!((Object)this instanceof LbcBoatImmunity boat)) return;

        if (boat.lbc_getFireSuppressTicks() <= 0) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;

        // Only suppress on the client (this is purely a visual concern)
        if (!((Entity)(Object)this).getEntityWorld().isClient()) return;

        cir.setReturnValue(false);
    }
}
