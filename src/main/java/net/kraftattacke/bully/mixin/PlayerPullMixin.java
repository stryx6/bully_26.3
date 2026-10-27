package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Pull replaces the melee knockback instead of fighting it. The attack event fires before knockback, so Pull is
 * applied here in attack() TAIL (after knockback) using the target's speed captured in attack() HEAD (before it):
 * a standing target stays put (knockback cancelled), a moving one is dragged toward the attacker, faster the quicker
 * they were moving.
 */
@Mixin(Player.class)
public abstract class PlayerPullMixin {
	@Unique private static final double BULLY_PULL_MOVING_THRESHOLD = 0.08; // below this horizontal speed = standing
	@Unique private static final double BULLY_PULL_SPEED_PER_LEVEL = 0.2;   // pull a bit faster than the target's speed

	@Unique private Vec3 bully$pullPreHitVelocity;
	@Unique private int bully$pullLevel;

	@Inject(method = "attack", at = @At("HEAD"))
	private void bully$capturePull(Entity target, CallbackInfo ci) {
		bully$pullPreHitVelocity = null;
		Player self = (Player) (Object) this;
		if (self.level().isClientSide() || !(target instanceof LivingEntity living)) {
			return;
		}
		int level = bully$pullLevel(self.getMainHandItem(), self);
		if (level > 0) {
			bully$pullLevel = level;
			bully$pullPreHitVelocity = living.getDeltaMovement(); // their movement before knockback overwrites it
		}
	}

	@Inject(method = "attack", at = @At("TAIL"))
	private void bully$applyPull(Entity target, CallbackInfo ci) {
		Vec3 preHit = bully$pullPreHitVelocity;
		bully$pullPreHitVelocity = null;
		if (preHit == null || !(target instanceof LivingEntity living)) {
			return;
		}
		Player self = (Player) (Object) this;
		double speed = Math.sqrt(preHit.x * preHit.x + preHit.z * preHit.z); // horizontal speed only
		if (speed < BULLY_PULL_MOVING_THRESHOLD) {
			living.setDeltaMovement(preHit); // standing still: undo the knockback, keep them where they were
			return;
		}
		double dx = self.getX() - living.getX();
		double dz = self.getZ() - living.getZ();
		double flat = Math.sqrt(dx * dx + dz * dz);
		if (flat < 1.0e-4) {
			return; // practically on the same spot
		}
		// Redirect their momentum toward the attacker, scaled a little above their own speed.
		double pullSpeed = speed * (1.0 + BULLY_PULL_SPEED_PER_LEVEL * bully$pullLevel);
		living.setDeltaMovement(dx / flat * pullSpeed, Math.min(0.4, preHit.y), dz / flat * pullSpeed);
	}

	@Unique
	private static int bully$pullLevel(ItemStack stack, Player player) {
		return EnchantmentHelper.getItemEnchantmentLevel(
				player.level().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
						.getOrThrow(ModEnchantments.PULL_KEY), stack);
	}
}
