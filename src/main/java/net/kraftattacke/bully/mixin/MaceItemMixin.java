package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MaceItem.class)
public abstract class MaceItemMixin {
	// Mace Guard cuts the fall-height bonus, so the saved damage grows with the attacker's fall.
	private static final float REDUCTION_PER_LEVEL = 0.04F;

	@Inject(method = "getAttackDamageBonus", at = @At("RETURN"), cancellable = true)
	private void bully$maceGuard(Entity target, float damage, DamageSource source, CallbackInfoReturnable<Float> cir) {
		if (!(target instanceof LivingEntity living) || cir.getReturnValueF() <= 0.0F) {
			return;
		}
		int levels = ModEnchantments.getTotalMaceGuardLevel(living);
		if (levels > 0) {
			cir.setReturnValue(cir.getReturnValueF() * Math.max(0.0F, 1.0F - REDUCTION_PER_LEVEL * levels));
		}
	}
}
