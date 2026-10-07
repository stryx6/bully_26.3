package net.kraftattacke.bully.item;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.MaceItem;

public class ScytheItem extends MaceItem {
	private static final ResourceKey<DamageType> SCYTHE_MAGIC_DAMAGE = ResourceKey.create(
			Registries.DAMAGE_TYPE,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "antimatter_scythe_magic")
	);

	public ScytheItem(Properties properties) {
		super(properties);
	}

	@Override
	public DamageSource getItemDamageSource(LivingEntity attacker) {
		return attacker.damageSources().source(SCYTHE_MAGIC_DAMAGE, attacker);
	}
}
