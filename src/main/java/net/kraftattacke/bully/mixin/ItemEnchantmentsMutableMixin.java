package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Disabled Bully enchantments can never be added to an item (commands, anvil, enchanting table, creative books). */
@Mixin(ItemEnchantments.Mutable.class)
public abstract class ItemEnchantmentsMutableMixin {
	@Inject(method = "set", at = @At("HEAD"), cancellable = true)
	private void bully$blockSet(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
		if (ModEnchantments.isDisabled(enchantment)) {
			ci.cancel();
		}
	}

	@Inject(method = "upgrade", at = @At("HEAD"), cancellable = true)
	private void bully$blockUpgrade(Holder<Enchantment> enchantment, int level, CallbackInfo ci) {
		if (ModEnchantments.isDisabled(enchantment)) {
			ci.cancel();
		}
	}
}
