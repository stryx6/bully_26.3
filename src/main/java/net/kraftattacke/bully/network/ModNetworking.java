package net.kraftattacke.bully.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;

public final class ModNetworking {
	private ModNetworking() {
	}

	public static void register() {
		PayloadTypeRegistry.clientboundPlay().register(MagicWandBeamPayload.TYPE, MagicWandBeamPayload.CODEC);
	}
}
