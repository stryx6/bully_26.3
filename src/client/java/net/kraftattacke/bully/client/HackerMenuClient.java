package net.kraftattacke.bully.client;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kraftattacke.bully.hackermenu.HackerMenuOpenPayload;
import net.kraftattacke.bully.hackermenu.HackerMenus;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.MenuScreens;
import com.mojang.blaze3d.platform.InputConstants;

public final class HackerMenuClient {
    private HackerMenuClient() { }

    public static void register() {
        MenuScreens.register(HackerMenus.MAIN, HackerMenuScreen::new);
        MenuScreens.register(HackerMenus.COMMAND, HackerMenuScreen::new);
        MenuScreens.register(HackerMenus.POTION, HackerMenuScreen::new);
        KeyMapping key = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.hackermenu.debugger", InputConstants.KEY_END, KeyMapping.Category.MISC));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (key.consumeClick()) {
                if (client.player != null && client.gui.screen() == null
                        && ClientPlayNetworking.canSend(HackerMenuOpenPayload.TYPE)) {
                    ClientPlayNetworking.send(new HackerMenuOpenPayload());
                }
            }
        });
    }
}
