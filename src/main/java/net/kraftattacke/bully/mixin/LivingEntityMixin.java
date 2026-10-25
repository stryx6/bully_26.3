package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.effect.ModEffects;
import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
	private static final Identifier ANTIMATTER_ELYTRA_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "antimatter_elytra");

	// Boost horizontal glide speed when using the antimatter elytra.
	private static final double HORIZONTAL_GLIDE_MULTIPLIER = 1.0;

	@Inject(method = "travel", at = @At("TAIL"))
	private void bully$boostAntimatterElytra(Vec3 input, CallbackInfo ci) {
		LivingEntity self = (LivingEntity)(Object)this;
		if (!self.isFallFlying()) {
			return;
		}

		if (!self.getItemBySlot(EquipmentSlot.CHEST).typeHolder().is(ANTIMATTER_ELYTRA_ID)) {
			return;
		}

		Vec3 out = self.getDeltaMovement();
		self.setDeltaMovement(new Vec3(out.x * HORIZONTAL_GLIDE_MULTIPLIER, out.y, out.z * HORIZONTAL_GLIDE_MULTIPLIER));
	}

	@Inject(method = "actuallyHurt", at = @At("TAIL"))
	private void bully$pullTarget(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount, CallbackInfo ci) {
		LivingEntity target = (LivingEntity)(Object)this;

		Entity attackerEntity = source.getEntity();
		if (!(attackerEntity instanceof LivingEntity attacker)) {
			return;
		}

		ItemStack weapon = source.getWeaponItem();
		if (weapon == null || weapon.isEmpty()) {
			return;
		}

		Holder.Reference<Enchantment> pull = ModEnchantments.getPull(level.registryAccess());
		int enchantLevel = EnchantmentHelper.getItemEnchantmentLevel(pull, weapon);
		if (enchantLevel <= 0) {
			return;
		}

		double dx = attacker.getX() - target.getX();
		double dy = attacker.getEyeY() - target.getEyeY();
		double dz = attacker.getZ() - target.getZ();
		double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
		if (distance < 0.0001D) {
			return;
		}

		double strength = 0.18D + (0.08D * (enchantLevel - 1));
		Vec3 pullVector = new Vec3(
				(dx / distance) * strength,
				(dy / distance) * strength * 0.45D,
				(dz / distance) * strength
		);
		target.setDeltaMovement(target.getDeltaMovement().add(pullVector));
	}

	@ModifyVariable(method = "heal", at = @At("HEAD"), argsOnly = true)
	private float bully$grievousWoundsHeal(float amount) {
		return amount * ModEffects.healMultiplier((LivingEntity)(Object)this);
	}

	// Covers golden apples and every other Regeneration source.
	@Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
	private void bully$blockRegeneration(MobEffectInstance instance, CallbackInfoReturnable<Boolean> cir) {
		if (instance.is(MobEffects.REGENERATION) && ((LivingEntity)(Object)this).hasEffect(ModEffects.GRIEVOUS_WOUNDS)) {
			cir.setReturnValue(false);
		}
	}

	// Antimatter scythe uses vanilla damage typing on 1.21.1; Sharpness will apply normally.
}
