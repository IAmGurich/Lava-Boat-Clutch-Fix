package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(AbstractBoatEntity.class)
public abstract class BoatEntityMixin implements LbcBoatImmunity {

    @Unique private int     lbc_immunityTicks     = 0;
    @Unique private boolean lbc_wasInLavaLastTick = false;

    @Unique private int     lbc_fireParticleSuppressTicks = 0;

    @Unique private boolean lbc_wasInLavaLastTickClient = false;

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

        AbstractBoatEntity self = (AbstractBoatEntity)(Object)this;

        if (self.getEntityWorld().isClient()) {
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
            self.setFireTicks(0);
            lbc_immunityTicks--;
        }

        if (inLavaNow) {
            Vec3d vel = self.getVelocity();
            if (vel.y < 0.0) {
                self.setVelocity(vel.x, 0.0, vel.z);
                self.velocityModified = true;
            }
        }
    }

    @Unique
    private static boolean lbc_isLavaBelow(AbstractBoatEntity boat) {
        BlockPos below = boat.getBlockPos().down();
        return boat.getEntityWorld()
                   .getBlockState(below)
                   .getFluidState()
                   .isIn(FluidTags.LAVA);
    }
}
