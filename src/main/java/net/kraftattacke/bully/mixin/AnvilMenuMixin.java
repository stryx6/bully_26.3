package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.EnchantingContext;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AnvilMenu.class)
public abstract class AnvilMenuMixin {
	@Inject(method = "createResult", at = @At("HEAD"))
	private void bully$setAnvilTarget(CallbackInfo ci) {
		// Slot 0 is the target item being modified in the anvil.
		ItemStack target = ((ItemCombinerMenuAccessor) this).bully$getInputSlots().getItem(0);
		EnchantingContext.setTargetStack(target);
	}

	@Inject(method = "createResult", at = @At("RETURN"))
	private void bully$clearAnvilTarget(CallbackInfo ci) {
		EnchantingContext.clearTargetStack();
	}
}
