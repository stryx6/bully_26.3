package net.kraftattacke.bully.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kraftattacke.bully.portable.CyclePortablePayload;
import net.kraftattacke.bully.portable.OpenPortablePayload;
import net.kraftattacke.bully.portable.PortableInfoPayload;
import net.minecraft.client.KeyMapping;

/** Utility-menu keybind + client-side state the screen/mouse mixins read (portable flag, highlight slots). */
public final class PortableClient {
	private static boolean pending;
	private static int portableContainerId = -1;
	private static int currentSlot = -1;
	private static int prevSlot = -1;
	private static int nextSlot = -1;

	private PortableClient() { }

	public static void register() {
		KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.utilitymenu.utility_menu", InputConstants.UNKNOWN.getValue(), UtilityMenuClient.BULLY_CATEGORY));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (key.consumeClick()) {
				if (client.player != null && client.gui.screen() == null
						&& ClientPlayNetworking.canSend(OpenPortablePayload.TYPE)) {
					pending = true;
					ClientPlayNetworking.send(new OpenPortablePayload());
				}
			}
		});
		ClientPlayNetworking.registerGlobalReceiver(PortableInfoPayload.TYPE, (payload, context) -> {
			currentSlot = payload.currentSlot();
			prevSlot = payload.prevSlot();
			nextSlot = payload.nextSlot();
		});
	}

	/** Called from the screen mixin's init: claims a pending portable open for this container, returns whether portable. */
	public static boolean resolve(int containerId) {
		if (pending) {
			portableContainerId = containerId;
			pending = false;
		}
		return containerId == portableContainerId;
	}

	/** Non-consuming check used by the highlight render. */
	public static boolean isPortable(int containerId) {
		return containerId == portableContainerId;
	}

	/** True between sending a cycle request and the replacement screen opening — used to skip the mouse re-grab. */
	public static boolean isPending() {
		return pending;
	}

	public static void sendCycle(int direction) {
		if (ClientPlayNetworking.canSend(CyclePortablePayload.TYPE)) {
			pending = true;
			ClientPlayNetworking.send(new CyclePortablePayload(direction));
		}
	}

	public static int currentSlot() { return currentSlot; }
	public static int prevSlot() { return prevSlot; }
	public static int nextSlot() { return nextSlot; }
}
