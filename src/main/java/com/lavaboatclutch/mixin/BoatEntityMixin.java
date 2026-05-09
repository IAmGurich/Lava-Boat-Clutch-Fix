package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractBoat.class)
public abstract class BoatEntityMixin implements LbcBoatImmunity {

    @Unique private int     lbc_immunityTicks     = 0;
    @Unique private boolean lbc_wasInLavaLastTick = false;

    @Unique private int     lbc_fireParticleSuppressTicks = 0;
    @Unique private boolean lbc_wasInLavaLastTickClient   = false;

    @Override public int     lbc_getImmunityTicks()              { return lbc_immunityTicks; }
    @Override public void    lbc_setImmunityTicks(int t)         { lbc_immunityTicks = t; }
    @Override public boolean lbc_wasInLavaLastTick()             { return lbc_wasInLavaLastTick; }
    @Override public void    lbc_setWasInLavaLastTick(boolean v) { lbc_wasInLavaLastTick = v; }
    @Override public int     lbc_getFireSuppressTicks()          { return lbc_fireParticleSuppressTicks; }
    @Override public void    lbc_setFireSuppressTicks(int t)     { lbc_fireParticleSuppressTicks = t; }

    @Inject(method = "tick", at = @At("HEAD"))
    private void lbc_onTick(CallbackInfo ci) {
        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;

        AbstractBoat self = (AbstractBoat)(Object)this;

        if (self.level().isClientSide()) {
            boolean inLavaNow = self.isInLava() || lbc_isLavaBelow(self);
            if (inLavaNow && !lbc_wasInLavaLastTickClient) {
                lbc_fireParticleSuppressTicks = 4;
            } else if (lbc_fireParticleSuppressTicks > 0) {
                lbc_fireParticleSuppressTicks--;
            }
            lbc_wasInLavaLastTickClient = inLavaNow;
            return;
        }

        boolean inLavaNow = self.isInLava() || lbc_isLavaBelow(self);

        if (!inLavaNow && lbc_immunityTicks == 0) {
            lbc_wasInLavaLastTick = false;
            return;
        }

        if (inLavaNow && !lbc_wasInLavaLastTick) {
            lbc_immunityTicks = cfg.lavaImmunityTicks;
            LavaBoatClutchMod.LOGGER.debug(
                "[LavaBoatClutch] Lava contact — immunity {} ticks", lbc_immunityTicks);
        }

        lbc_wasInLavaLastTick = inLavaNow;

        if (lbc_immunityTicks > 0) {

            self.setRemainingFireTicks(0);
            lbc_immunityTicks--;
        }

        if (inLavaNow) {

            Vec3 vel = self.getDeltaMovement();
            if (vel.y < 0.0) {

                self.setDeltaMovement(vel.x, 0.0, vel.z);

            }
        }
    }

    @Unique
    private static boolean lbc_isLavaBelow(AbstractBoat boat) {

        BlockPos below = boat.blockPosition().below();
        return boat.level()
                   .getBlockState(below)
                   .getFluidState()
                   .is(FluidTags.LAVA);
    }
}
