package net.kraftattacke.bully.mixin;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Books of a disabled enchantment come out empty (and identical, which vanilla rejects): drop them before they are added. */
@Mixin(targets = "net.minecraft.world.item.CreativeModeTab$ItemDisplayBuilder")
public abstract class CreativeTabBookMixin {
	@Inject(method = "accept", at = @At("HEAD"), cancellable = true)
	private void bully$skipEmptyBooks(ItemStack stack, CreativeModeTab.TabVisibility visibility, CallbackInfo ci) {
		if (stack.is(Items.ENCHANTED_BOOK)
				&& stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY).isEmpty()) {
			ci.cancel();
		}
	}
}
