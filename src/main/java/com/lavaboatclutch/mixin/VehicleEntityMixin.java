package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.vehicle.AbstractBoatEntity;
import net.minecraft.entity.vehicle.VehicleEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadLocalRandom;


@Mixin(VehicleEntity.class)
public abstract class VehicleEntityMixin {

    @Unique
    private @Nullable ItemEntity lbc_pendingDrop = null;

    @Inject(
        method = "damage(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)Z",
        at = @At("HEAD"),
        cancellable = true
    )
    private void lbc_onDamage(ServerWorld world, DamageSource source, float amount,
                               CallbackInfoReturnable<Boolean> cir) {
        if (!(((Object) this) instanceof AbstractBoatEntity)) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;
        if (!source.isIn(DamageTypeTags.IS_FIRE)) return;

        LbcBoatImmunity immunity = (LbcBoatImmunity)(Object)this;
        if (immunity.lbc_getImmunityTicks() > 0) {
            LavaBoatClutchMod.LOGGER.debug(
                "[LavaBoatClutch] Blocked fire damage (remaining={})",
                immunity.lbc_getImmunityTicks());
            cir.setReturnValue(false);
        }
    }

    @Redirect(
        method = "killAndDropItem(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/Item;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/entity/vehicle/VehicleEntity;dropStack(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/ItemStack;)Lnet/minecraft/entity/ItemEntity;"
        )
    )
    private @Nullable ItemEntity lbc_captureDropStack(VehicleEntity self,
                                                       ServerWorld world,
                                                       ItemStack stack) {
        @Nullable ItemEntity ie = self.dropStack(world, stack);
        lbc_pendingDrop = ie;
        return ie;
    }

    @Inject(
        method = "killAndDropItem(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/item/Item;)V",
        at = @At("TAIL")
    )
    private void lbc_onKillAndDropItem(ServerWorld world, Item item, CallbackInfo ci) {
        ItemEntity ie = lbc_pendingDrop;
        lbc_pendingDrop = null;

        if (!(((Object) this) instanceof AbstractBoatEntity boat)) return;

        LavaBoatClutchConfig cfg = LavaBoatClutchMod.getConfig();
        if (cfg == null || !cfg.enableMod) return;

        LbcBoatImmunity immunity = (LbcBoatImmunity)(Object)this;
        if (!immunity.lbc_wasInLavaLastTick()) return;

        if (ie == null || ie.isRemoved()) return;

        // Random mode bypasses the bounceY <= 0 check since Y will be randomized
        if (!cfg.isRandomMode()) {
            float bounceY = cfg.getEffectiveBounce();
            if (bounceY <= 0.0f) return;
        }

        float cfgBounceX = cfg.isCustomMode() ? cfg.bounceDropX : 0f;
        float cfgBounceZ = cfg.isCustomMode() ? cfg.bounceDropZ : 0f;

        double safeY = boat.getY() + 0.5;

        LavaBoatClutchMod.LOGGER.debug(
            "[LavaBoatClutch] killAndDropItem — applying bounce " +
            "(bounceY={}, X={}, Z={}, safeY={}, mode={})",
            cfg.getEffectiveBounce(), cfgBounceX, cfgBounceZ, safeY,
            cfg.bounceDropMode);

        lbc_applyBounce(ie, cfg, cfgBounceX, cfgBounceZ, safeY);

        LavaBoatClutchMod.LOGGER.debug(
            "[LavaBoatClutch] Bounce applied at {},{},{}",
            (int)boat.getX(), (int)boat.getY(), (int)boat.getZ());
    }

    @Unique
    private static void lbc_applyBounce(ItemEntity ie, LavaBoatClutchConfig cfg,
                                         float cfgBounceX, float cfgBounceZ,
                                         double safeY) {
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        // 1. Velocity
        switch (cfg.bounceDropMode) {
            case DEFAULT -> {
                // Vanilla-like: fixed Y, small random X/Z
                double randX = rng.nextDouble() * 0.2 - 0.1;
                double randZ = rng.nextDouble() * 0.2 - 0.1;
                ie.setVelocity(randX, LavaBoatClutchConfig.VANILLA_BOUNCE_DROP, randZ);
            }
            case CUSTOM -> {
                // Fully configurable X, Y, Z
                ie.setVelocity(cfgBounceX, cfg.bounceDrop, cfgBounceZ);
            }
            case RANDOM -> {
                // Fully random velocity in a wider range
                double randX = rng.nextDouble() * 0.6 - 0.3;
                double randZ = rng.nextDouble() * 0.6 - 0.3;
                float  randY = 0.05f + rng.nextFloat() * 0.35f;
                ie.setVelocity(randX, randY, randZ);
            }
        }
        ie.velocityModified = true;

        // 2. Телепортация выше поверхности лавы
        if (ie.getY() < safeY) {
            ie.setPos(ie.getX(), safeY, ie.getZ());
        }

        // 3. Иммунитет к огню: отрицательные fire ticks → entity в огне/лаве
        //    |fireTicks| тиков без возгорания (vanilla механика)
        ie.setFireTicks(-80);
    }
}
