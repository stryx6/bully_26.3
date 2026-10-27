package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	// Runs after every successful melee hit, for player and mob targets alike.
	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void bully$applySwordCoating(LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
		if (target.level() instanceof ServerLevel level) {
			SwordCoating.onHit(level, (ItemStack)(Object)this, target, attacker);
		}
	}
}
