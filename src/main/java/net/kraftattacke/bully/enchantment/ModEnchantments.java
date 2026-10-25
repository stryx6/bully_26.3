package net.kraftattacke.bully.enchantment;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public final class ModEnchantments {
	public static Holder<Enchantment> MACE_GUARD;
	public static final ResourceKey<Enchantment> MACE_GUARD_KEY = ResourceKey.create(
			Registries.ENCHANTMENT,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "mace_guard")
	);

	public static final ResourceKey<Enchantment> PULL_KEY = ResourceKey.create(
			Registries.ENCHANTMENT,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "pull")
	);

	public static final ResourceKey<Enchantment> HUNGER_LEECH_KEY = ResourceKey.create(
			Registries.ENCHANTMENT,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "hunger_leech")
	);

	private ModEnchantments() {
	}

	public static Holder.Reference<Enchantment> getMaceGuard(RegistryAccess registryAccess) {
		HolderGetter<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		return enchantments.getOrThrow(MACE_GUARD_KEY);
	}

	/** Mace Guard levels summed over all four armor pieces (max 16). */
	public static int getTotalMaceGuardLevel(LivingEntity entity) {
		Holder.Reference<Enchantment> maceGuard = getMaceGuard(entity.level().registryAccess());
		int total = 0;
		for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
			total += EnchantmentHelper.getItemEnchantmentLevel(maceGuard, entity.getItemBySlot(slot));
		}
		return total;
	}

	public static Holder.Reference<Enchantment> getPull(RegistryAccess registryAccess) {
		HolderGetter<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		return enchantments.getOrThrow(PULL_KEY);
	}
}
