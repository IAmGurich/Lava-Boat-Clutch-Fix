package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;" +
                 "Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lbc_onDamage(ServerLevel level,
                               DamageSource source,
                               float amount,
                               CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof AbstractBoat)) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;
        if (!source.is(DamageTypeTags.IS_FIRE)) return;

        LbcBoatImmunity immunity = (LbcBoatImmunity) (Object) this;
        if (immunity.lbc_getImmunityTicks() > 0) {
            LavaBoatClutchMod.LOGGER.debug(
                "[LavaBoatClutch] Blocked fire damage (immunityTicks={})",
                immunity.lbc_getImmunityTicks());
            cir.setReturnValue(false);
        }
    }

    @Inject(
        method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;" +
                 "Lnet/minecraft/world/damagesource/DamageSource;F)Z",
        at = @At("RETURN")
    )
    private void lbc_onHurtServerReturn(ServerLevel level,
                                         DamageSource source,
                                         float amount,
                                         CallbackInfoReturnable<Boolean> cir) {

        if (!(((Object) this) instanceof AbstractBoat boat)) return;

        if (!source.is(DamageTypeTags.IS_FIRE)) return;

        if (!Boolean.TRUE.equals(cir.getReturnValue())) return;

        if (!boat.isRemoved()) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;

        LbcBoatImmunity immunity = (LbcBoatImmunity) (Object) this;
        if (!immunity.lbc_wasInLavaLastTick()) return;

        float bounceY = cfg.getEffectiveBounce();
        if (bounceY <= 0.0f) return;

        boolean vanillaMode = cfg.isVanillaMode();
        float cfgBounceX = vanillaMode ? 0f : cfg.bounceDropX;
        float cfgBounceZ = vanillaMode ? 0f : cfg.bounceDropZ;

        double safeY = boat.getY() + 0.5;

        AABB searchBox = AABB.ofSize(boat.position(), 3.0, 3.0, 3.0);
        List<ItemEntity> freshDrops = level.getEntitiesOfClass(
            ItemEntity.class,
            searchBox,
            ie -> !ie.isRemoved() && ie.getAge() < 2
        );

        if (freshDrops.isEmpty()) {
            LavaBoatClutchMod.LOGGER.debug(
                "[LavaBoatClutch] Boat destroyed in lava at {},{},{} " +
                "— no fresh ItemEntity found (probably no item drop for this boat type)",
                (int) boat.getX(), (int) boat.getY(), (int) boat.getZ());
            return;
        }

        LavaBoatClutchMod.LOGGER.debug(
            "[LavaBoatClutch] Applying bounce to {} item(s) at {},{},{} " +
            "(bounceY={}, mode={})",
            freshDrops.size(),
            (int) boat.getX(), (int) boat.getY(), (int) boat.getZ(),
            bounceY,
            vanillaMode ? "vanilla" : "custom");

        for (ItemEntity ie : freshDrops) {
            lbc_applyBounce(ie, bounceY, cfgBounceX, cfgBounceZ, safeY, vanillaMode);
        }
    }

    @Unique
    private static void lbc_applyBounce(ItemEntity ie,
                                         float bounceY,
                                         float cfgBounceX,
                                         float cfgBounceZ,
                                         double safeY,
                                         boolean vanillaMode) {

        if (vanillaMode) {

            ThreadLocalRandom rng = ThreadLocalRandom.current();
            double randX = rng.nextDouble() * 0.2 - 0.1;
            double randZ = rng.nextDouble() * 0.2 - 0.1;
            ie.setDeltaMovement(randX, bounceY, randZ);
        } else {

            ie.setDeltaMovement(cfgBounceX, bounceY, cfgBounceZ);
        }

        if (ie.getY() < safeY) {
            ie.setPos(ie.getX(), safeY, ie.getZ());
        }

        ie.setRemainingFireTicks(-80);
    }
}
