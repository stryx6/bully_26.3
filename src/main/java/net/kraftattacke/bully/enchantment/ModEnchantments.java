package net.kraftattacke.bully.enchantment;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.enchantment.Enchantment;

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

	private ModEnchantments() {
	}

	public static Holder.Reference<Enchantment> getMaceGuard(RegistryAccess registryAccess) {
		HolderGetter<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		return enchantments.getOrThrow(MACE_GUARD_KEY);
	}

	public static Holder.Reference<Enchantment> getPull(RegistryAccess registryAccess) {
		HolderGetter<Enchantment> enchantments = registryAccess.lookupOrThrow(Registries.ENCHANTMENT);
		return enchantments.getOrThrow(PULL_KEY);
	}
}
