package net.kraftattacke.bully.client;

import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.kraftattacke.bully.ModParticles;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public class BurningShieldParticle extends SingleQuadParticle {
	private final SpriteSet sprites;

	private BurningShieldParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
		super(level, x, y, z, xSpeed, ySpeed, zSpeed, sprites.first());
		this.sprites = sprites;
		this.friction = 0.92F;
		this.gravity = -0.02F;
		this.quadSize *= 0.42F + this.random.nextFloat() * 0.32F;
		this.lifetime = 14 + this.random.nextInt(10);
		this.hasPhysics = false;
	}

	public static void registerFactory() {
		ParticleProviderRegistry.getInstance().register(ModParticles.BURNING_SHIELD, Provider::new);
	}

	@Override
	public void tick() {
		super.tick();
		setSpriteFromAge(sprites);
		float fade = 1.0F - age / (float) lifetime;
		setAlpha(Mth.clamp(fade * 1.2F, 0.0F, 1.0F));
	}

	@Override
	public float getQuadSize(float tickProgress) {
		float ageProgress = Mth.clamp((age + tickProgress) / (float) lifetime, 0.0F, 1.0F);
		return quadSize * (1.0F + ageProgress * 0.35F);
	}

	@Override
	protected int getLightCoords(float tickProgress) {
		return 0xF000F0;
	}

	@Override
	protected Layer getLayer() {
		return Layer.TRANSLUCENT;
	}

	private record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, RandomSource random) {
			return new BurningShieldParticle(level, x, y, z, xSpeed, ySpeed + 0.015 + random.nextDouble() * 0.03, zSpeed, sprites);
		}
	}
}
