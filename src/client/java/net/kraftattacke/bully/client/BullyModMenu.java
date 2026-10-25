package net.kraftattacke.bully.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;

/** Registers the gear button next to Bully in the mod list; it opens the Cloth Config screen. */
public final class BullyModMenu implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return BullyConfigScreen::create;
	}
}
