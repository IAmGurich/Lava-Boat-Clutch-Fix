package com.lavaboatclutch.mixin;

import com.lavaboatclutch.LavaBoatClutchMod;
import com.lavaboatclutch.config.LavaBoatClutchConfig;
import com.lavaboatclutch.util.LbcBoatImmunity;
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

    /** Holds the ItemEntity dropped by killAndDropItem so the TAIL inject can modify it. */
    @Unique
    private @Nullable ItemEntity lbc_pendingDrop = null;

    // ── Fire damage immunity ───────────────────────────────────────────────────

    /**
     * Cancels fire damage to a boat that is still within its lava immunity window.
     * This prevents the boat from being destroyed immediately on lava contact,
     * giving the player time to land on its hitbox.
     */
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

    // ── Drop capture ───────────────────────────────────────────────────────────

    /**
     * Captures the ItemEntity reference produced by {@code dropStack} so the
     * TAIL inject below can apply bounce velocity to it.
     */
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

    // ── Drop bounce ────────────────────────────────────────────────────────────

    /**
     * After the boat item has been dropped into the lava, applies an upward
     * velocity kick (and optional horizontal drift) so the drop pops above the
     * lava surface rather than sinking and burning.
     *
     * <p>Three modes are supported, controlled by {@link LavaBoatClutchConfig#dropBounceMode}:</p>
     * <ul>
     *   <li><b>DEFAULT</b> — mimics vanilla: Y = {@value LavaBoatClutchConfig#VANILLA_BOUNCE_DROP},
     *       X/Z ∈ [-0.1, 0.1] (uniform random).</li>
     *   <li><b>CUSTOM</b> — user-defined fixed X, Y, Z velocities from the config.</li>
     *   <li><b>RANDOM</b> — fully randomised on every drop:
     *       Y ∈ [{@value LavaBoatClutchConfig#MIN_BOUNCE_DROP}, {@value LavaBoatClutchConfig#MAX_BOUNCE_DROP}),
     *       X/Z ∈ [{@value LavaBoatClutchConfig#MIN_BOUNCE_HORIZ}, {@value LavaBoatClutchConfig#MAX_BOUNCE_HORIZ}).</li>
     * </ul>
     */
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

        LavaBoatClutchConfig.DropBounceMode mode = cfg.dropBounceMode;

        // In CUSTOM mode: respect user setting of Y ≤ 0 as "no bounce"
        if (mode == LavaBoatClutchConfig.DropBounceMode.CUSTOM && cfg.bounceDrop <= 0.0f) return;

        LbcBoatImmunity immunity = (LbcBoatImmunity)(Object)this;
        if (!immunity.lbc_wasInLavaLastTick()) return;

        if (ie == null || ie.isRemoved()) return;

        double safeY = boat.getY() + 0.5;

        LavaBoatClutchMod.LOGGER.debug(
            "[LavaBoatClutch] killAndDropItem — applying bounce (mode={}, safeY={})",
            mode, safeY);

        lbc_applyBounce(ie, cfg, mode, safeY);

        LavaBoatClutchMod.LOGGER.debug(
            "[LavaBoatClutch] Bounce applied at {},{},{}",
            (int)boat.getX(), (int)boat.getY(), (int)boat.getZ());
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
                // Each axis independently randomised across the full configured range
                velY = rng.nextDouble(LavaBoatClutchConfig.MIN_BOUNCE_DROP,
                                      LavaBoatClutchConfig.MAX_BOUNCE_DROP);
                velX = rng.nextDouble(LavaBoatClutchConfig.MIN_BOUNCE_HORIZ,
                                      LavaBoatClutchConfig.MAX_BOUNCE_HORIZ);
                velZ = rng.nextDouble(LavaBoatClutchConfig.MIN_BOUNCE_HORIZ,
                                      LavaBoatClutchConfig.MAX_BOUNCE_HORIZ);
            }
            default -> {
                // DEFAULT — small random horizontal drift, fixed upward kick (vanilla behaviour)
                velX = rng.nextDouble() * 0.2 - 0.1;
                velY = LavaBoatClutchConfig.VANILLA_BOUNCE_DROP;
                velZ = rng.nextDouble() * 0.2 - 0.1;
            }
        }

        ie.setVelocity(velX, velY, velZ);
        ie.velocityModified = true;

        // Teleport the item above the lava surface so it cannot sink back in
        if (ie.getY() < safeY) {
            ie.setPos(ie.getX(), safeY, ie.getZ());
        }

        // Negative fire ticks = fire immunity duration (vanilla mechanic)
        ie.setFireTicks(-80);
    }
}
