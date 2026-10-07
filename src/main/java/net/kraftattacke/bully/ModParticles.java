package net.kraftattacke.bully;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModParticles {
	public static final SimpleParticleType BURNING_SHIELD = Registry.register(
			BuiltInRegistries.PARTICLE_TYPE,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "burning_shield"),
			FabricParticleTypes.simple()
	);

	private ModParticles() {
	}

	public static void register() {
		Bully.LOGGER.info("Registering particles for {}", Bully.MOD_ID);
	}
}
