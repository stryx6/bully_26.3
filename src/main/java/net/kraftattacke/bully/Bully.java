package net.kraftattacke.bully;

import net.kraftattacke.bully.block.ModBlocks;
import net.kraftattacke.bully.effect.ModEffects;
import net.kraftattacke.bully.item.SwordCoating;
import net.kraftattacke.bully.item.ModItems;
import net.kraftattacke.bully.item.ModTools;
import net.kraftattacke.bully.network.ModNetworking;
import net.kraftattacke.bully.villager.ModVillagers;
import net.fabricmc.api.ModInitializer;
import net.kraftattacke.bully.event.AttackEvents;
import net.kraftattacke.bully.event.MagicWandLightEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Bully implements ModInitializer {
	public static final String MOD_ID = "bully";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		net.kraftattacke.bully.config.BullyConfig.load();
		net.kraftattacke.bully.item.MagicWandItem.applyConfig();
		net.kraftattacke.bully.utilitymenu.UtilityMenus.register();
		net.kraftattacke.bully.portable.PortableStations.register();
		ModNetworking.register();
		ModSounds.register();
		ModParticles.register();
		ModBlocks.registerModBlocks();
		ModItems.registerItemGroups();
		ModItems.register();
		ModTools.register();
		ModEffects.register();
		SwordCoating.register();
		ModVillagers.register();
		AttackEvents.register();
		MagicWandLightEvents.register();
		net.kraftattacke.bully.command.ModCommands.register();

		LOGGER.info("Bully initialized");
	}
}
