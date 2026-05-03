package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityFireMixin {

    @Inject(method = "doesRenderOnFire()Z", at = @At("HEAD"), cancellable = true)
    private void lbc_suppressFireRender(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object)this instanceof LbcBoatImmunity boat)) return;

        if (boat.lbc_getFireSuppressTicks() <= 0) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;

        if (!((Entity)(Object)this).getEntityWorld().isClient()) return;

        cir.setReturnValue(false);
    }
}
