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

        if (immunity.lbc_getImmunityTicks() == 0 && !immunity.lbc_isImmunityGranted()) {
            immunity.lbc_setImmunityTicks(cfg.lavaImmunityTicks);
            immunity.lbc_setImmunityGranted(true);
            immunity.lbc_setWasInLavaLastTick(true);
        }

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

        if (!cir.getReturnValueZ()) return;

        if (!boat.isRemoved()) return;

        if (!source.is(DamageTypeTags.IS_FIRE)) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;

        LbcBoatImmunity immunity = (LbcBoatImmunity) (Object) this;
        if (!immunity.lbc_wasInLavaLastTick()) return;

        LavaBoatClutchConfig.DropBounceMode mode = cfg.dropBounceMode;

        if (mode == LavaBoatClutchConfig.DropBounceMode.CUSTOM && cfg.bounceDrop <= 0.0f) return;

        double safeY = boat.getY() + 0.5;

        AABB searchBox = AABB.ofSize(boat.position(), 3.0, 3.0, 3.0);
        List<ItemEntity> freshDrops = level.getEntitiesOfClass(
            ItemEntity.class,
            searchBox,
            ie -> !ie.isRemoved() && ie.getAge() < 2
        );

        if (freshDrops.isEmpty()) {
            LavaBoatClutchMod.LOGGER.debug(
                "[LavaBoatClutch] Boat destroyed in lava at {},{},{}" +
                " — no fresh ItemEntity found",
                (int) boat.getX(), (int) boat.getY(), (int) boat.getZ());
            return;
        }

        for (ItemEntity ie : freshDrops) {
            lbc_applyBounce(ie, cfg, mode, safeY);
        }
    }

    @Unique
    private static void lbc_applyBounce(ItemEntity ie,
                                         LavaBoatClutchConfig cfg,
                                         LavaBoatClutchConfig.DropBounceMode mode,
                                         double safeY) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();
        final double velX, velY, velZ;

        switch (mode) {
            case CUSTOM -> {
                velX = cfg.bounceDropX;
                velY = cfg.bounceDrop;
                velZ = cfg.bounceDropZ;
            }
            case RANDOM -> {
                velY = rng.nextDouble(LavaBoatClutchConfig.MIN_BOUNCE_DROP,
                                      LavaBoatClutchConfig.MAX_BOUNCE_DROP);
                velX = rng.nextDouble(LavaBoatClutchConfig.MIN_BOUNCE_HORIZ,
                                      LavaBoatClutchConfig.MAX_BOUNCE_HORIZ);
                velZ = rng.nextDouble(LavaBoatClutchConfig.MIN_BOUNCE_HORIZ,
                                      LavaBoatClutchConfig.MAX_BOUNCE_HORIZ);
            }
            default -> {
                velX = rng.nextDouble() * 0.2 - 0.1;
                velY = LavaBoatClutchConfig.VANILLA_BOUNCE_DROP;
                velZ = rng.nextDouble() * 0.2 - 0.1;
            }
        }

        ie.setDeltaMovement(velX, velY, velZ);

        if (ie.getY() < safeY) {
            ie.setPos(ie.getX(), safeY, ie.getZ());
        }

        ie.setRemainingFireTicks(-80);
    }
}
