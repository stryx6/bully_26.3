package net.kraftattacke.bully;

import net.fabricmc.api.ClientModInitializer;
import net.kraftattacke.bully.client.BurningShieldParticle;
import net.kraftattacke.bully.client.MagicWandVisuals;

public class BullyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		net.kraftattacke.bully.client.HackerMenuClient.register();
		BurningShieldParticle.registerFactory();
		MagicWandVisuals.register();
	}
}
