package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.EnchantingContext;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.stream.Stream;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {
	@Inject(method = "getAvailableEnchantmentResults", at = @At("HEAD"))
	private static void bully$setEnchantingTarget(int level, ItemStack stack, Stream<Holder<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
		EnchantingContext.setTargetStack(stack);
	}

	@Inject(method = "getAvailableEnchantmentResults", at = @At("RETURN"))
	private static void bully$clearEnchantingTarget(int level, ItemStack stack, Stream<Holder<Enchantment>> possibleEnchantments, CallbackInfoReturnable<List<EnchantmentInstance>> cir) {
		EnchantingContext.clearTargetStack();
	}
}
