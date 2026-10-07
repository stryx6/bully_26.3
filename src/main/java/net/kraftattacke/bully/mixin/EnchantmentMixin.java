package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.enchantment.EnchantingContext;
import net.kraftattacke.bully.enchantment.ModEnchantments;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public class EnchantmentMixin {
	private static final Identifier ANTIMATTER_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "antimatter_chestplate");
	private static final Identifier AETHERALD_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "aetherald_chestplate");
	private static final Identifier AURONYX_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "auronyx_chestplate");
	private static final Identifier FERALYTH_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "feralyth_chestplate");
	private static final Identifier FLUXITE_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "fluxite_chestplate");
	private static final Identifier MYSTRALITE_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "mystralite_chestplate");
	private static final Identifier NEBULAZUL_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "nebulazul_chestplate");
	private static final Identifier VOIDCRIST_CHESTPLATE_ID = Identifier.fromNamespaceAndPath("bully", "voidcrist_chestplate");
	private static final Identifier ANTIMATTER_SCYTHE_ID = Identifier.fromNamespaceAndPath("bully", "antimatter_scythe");


	@Inject(method = "areCompatible", at = @At("HEAD"), cancellable = true)
	private static void bully$areCompatible(Holder<Enchantment> first, Holder<Enchantment> second, CallbackInfoReturnable<Boolean> cir) {
		ItemStack target = EnchantingContext.getTargetStack();
		if (target == null) {
			return;
		}

		if (isProtectionEnchantment(first) && isProtectionEnchantment(second) && isAntimatterChestplate(target)) {
			// Only for antimatter armor: allow the 4 protection enchantments to stack together.
			cir.setReturnValue(true);
			return;
		}

		if (isBreachAndSharpness(first) && isBreachAndSharpness(second) && isAntimatterScythe(target)) {
			// Only for the antimatter scythe: allow Density + Breach to stack together.
			cir.setReturnValue(true);
		}

		if (isDensityAndSharpness(first) && isDensityAndSharpness(second) && isAntimatterScythe(target)) {
			// Only for the antimatter scythe: allow Density + Breach to stack together.
			cir.setReturnValue(true);
		}

	}

	private static boolean isProtectionEnchantment(Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.PROTECTION)
				|| enchantment.is(Enchantments.FIRE_PROTECTION)
				|| enchantment.is(Enchantments.BLAST_PROTECTION)
				|| enchantment.is(Enchantments.PROJECTILE_PROTECTION);
	}

	private static boolean isCrossbowEnchantment(Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.MULTISHOT)
				|| enchantment.is(Enchantments.POWER)
				|| enchantment.is(Enchantments.FLAME)
				|| enchantment.is(Enchantments.INFINITY)
				|| enchantment.is(Enchantments.PIERCING);
	}

	private static boolean isAntimatterChestplate(ItemStack stack) {
		var holder = stack.typeHolder();
		return holder.is(ANTIMATTER_CHESTPLATE_ID)
		 		|| holder.is(AETHERALD_CHESTPLATE_ID)
				|| holder.is(AURONYX_CHESTPLATE_ID)
				|| holder.is(FERALYTH_CHESTPLATE_ID)
				|| holder.is(FLUXITE_CHESTPLATE_ID)
				|| holder.is(MYSTRALITE_CHESTPLATE_ID)
				|| holder.is(NEBULAZUL_CHESTPLATE_ID)
				|| holder.is(VOIDCRIST_CHESTPLATE_ID);
	}

	private static boolean isAntimatterArmor(ItemStack stack) {
		var holder = stack.typeHolder();
		return holder.is(ANTIMATTER_CHESTPLATE_ID)
				|| holder.is(AETHERALD_CHESTPLATE_ID)
				|| holder.is(AURONYX_CHESTPLATE_ID)
				|| holder.is(FERALYTH_CHESTPLATE_ID)
				|| holder.is(FLUXITE_CHESTPLATE_ID)
				|| holder.is(MYSTRALITE_CHESTPLATE_ID)
				|| holder.is(NEBULAZUL_CHESTPLATE_ID)
				|| holder.is(VOIDCRIST_CHESTPLATE_ID);
	}

	private static boolean isAntimatterScythe(ItemStack stack) {
		return stack.typeHolder().is(ANTIMATTER_SCYTHE_ID);
	}

	private static boolean isDensityAndSharpness(Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.DENSITY) || enchantment.is(Enchantments.SHARPNESS);
	}

	private static boolean isBreachAndSharpness(Holder<Enchantment> enchantment) {
		return enchantment.is(Enchantments.BREACH) || enchantment.is(Enchantments.SHARPNESS);
	}

	private static boolean isMaceGuard(Holder<Enchantment> enchantment) {
		return enchantment.is(ModEnchantments.MACE_GUARD);
	}

	private static boolean isMaceGuardAndProtection(Holder<Enchantment> first, Holder<Enchantment> second) {
		return (isMaceGuard(first) && isProtectionEnchantment(second))
				|| (isProtectionEnchantment(first) && isMaceGuard(second));
	}
}
