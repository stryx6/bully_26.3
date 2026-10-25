package net.kraftattacke.bully;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.alchemy.PotionContents;
import net.kraftattacke.bully.client.BurningShieldParticle;
import net.kraftattacke.bully.client.MagicWandVisuals;

public class BullyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		net.kraftattacke.bully.client.UtilityMenuClient.register();
		BurningShieldParticle.registerFactory();
		MagicWandVisuals.register();

		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			SwordCoating coating = stack.get(SwordCoating.TYPE);
			if (coating != null) {
				lines.add(Component.translatable("item.bully.sword_coating", coating.hits()).withStyle(ChatFormatting.DARK_PURPLE));
				PotionContents.addPotionTooltip(coating.contents().getAllEffects(), lines::add, SwordCoating.DURATION_SCALE, context.tickRate());
			}
		});
	}
}
