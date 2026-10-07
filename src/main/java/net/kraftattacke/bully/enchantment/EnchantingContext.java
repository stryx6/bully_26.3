package net.kraftattacke.bully.enchantment;

import net.minecraft.world.item.ItemStack;

/**
 * Thread-local context used by mixins to make enchantment compatibility checks item-aware.
 */
public final class EnchantingContext {
	private static final ThreadLocal<ItemStack> TARGET_STACK = new ThreadLocal<>();

	private EnchantingContext() {
	}

	public static void setTargetStack(ItemStack stack) {
		TARGET_STACK.set(stack);
	}

	public static void clearTargetStack() {
		TARGET_STACK.remove();
	}

	public static ItemStack getTargetStack() {
		return TARGET_STACK.get();
	}
}

