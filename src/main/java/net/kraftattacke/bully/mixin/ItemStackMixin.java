package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@org.spongepowered.asm.mixin.Shadow
	private int count;

	// With scythes switched off, every scythe stack (give, creative, crafting, saved worlds) becomes empty.
	@Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
	private void bully$removeDisabledScythes(net.minecraft.core.Holder<net.minecraft.world.item.Item> item, int count, net.minecraft.core.component.PatchedDataComponentMap components, CallbackInfo ci) {
		var features = net.kraftattacke.bully.config.BullyConfig.get().features;
		if ((!features.scythes && item.is(net.kraftattacke.bully.item.ModTools.SCYTHES_TAG))
				|| (!features.magicWand && item.is(net.minecraft.resources.Identifier.fromNamespaceAndPath("bully", "magic_wand")))) {
			this.count = 0;
		}
	}

	// Runs after every successful melee hit, for player and mob targets alike.
	@Inject(method = "hurtEnemy", at = @At("HEAD"))
	private void bully$applySwordCoating(LivingEntity target, LivingEntity attacker, CallbackInfoReturnable<Boolean> cir) {
		if (target.level() instanceof ServerLevel level) {
			SwordCoating.onHit(level, (ItemStack)(Object)this, target, attacker);
		}
	}
}
