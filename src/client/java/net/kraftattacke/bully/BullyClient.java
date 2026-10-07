package net.kraftattacke.bully;

import net.fabricmc.api.ClientModInitializer;
import net.kraftattacke.bully.client.BurningShieldParticle;
import net.kraftattacke.bully.client.MagicWandVisuals;

public class BullyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BurningShieldParticle.registerFactory();
		MagicWandVisuals.register();
	}
}
