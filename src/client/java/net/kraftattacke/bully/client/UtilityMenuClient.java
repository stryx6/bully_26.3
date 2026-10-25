package net.kraftattacke.bully.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kraftattacke.bully.utilitymenu.UtilityMenuOpenPayload;
import net.kraftattacke.bully.utilitymenu.UtilityMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import com.mojang.blaze3d.platform.InputConstants;

public final class UtilityMenuClient {
    private UtilityMenuClient() { }

    public static void register() {
        MenuScreens.register(UtilityMenus.MAIN, UtilityMenuScreen::new);
        MenuScreens.register(UtilityMenus.COMMAND, UtilityMenuScreen::new);
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.utilitymenu.debugger", InputConstants.UNKNOWN.getValue(), KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.player != null && client.gui.screen() == null
                        && ClientPlayNetworking.canSend(UtilityMenuOpenPayload.TYPE)) {
                    ClientPlayNetworking.send(new UtilityMenuOpenPayload());
                }
            }
        });
    }
}
