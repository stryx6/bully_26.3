package net.kraftattacke.bully;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.kraftattacke.bully.client.BurningShieldParticle;
import net.kraftattacke.bully.client.MagicWandVisuals;

public class BullyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		net.kraftattacke.bully.client.UtilityMenuClient.register();
		net.kraftattacke.bully.client.PortableClient.register();
		BurningShieldParticle.registerFactory();
		MagicWandVisuals.register();

		net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents.MODIFY_OUTPUT_ALL.register((group, entries) -> {
			java.util.function.Predicate<ItemStack> emptyBook = stack -> stack.is(Items.ENCHANTED_BOOK)
					&& stack.getOrDefault(DataComponents.STORED_ENCHANTMENTS, net.minecraft.world.item.enchantment.ItemEnchantments.EMPTY).isEmpty();
			entries.getDisplayStacks().removeIf(emptyBook);
			entries.getSearchTabStacks().removeIf(emptyBook);
		});

		var boostKey = net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper.registerKeyMapping(new net.minecraft.client.KeyMapping(
				"key.bully.elytra_boost", com.mojang.blaze3d.platform.InputConstants.KEY_R, net.kraftattacke.bully.client.UtilityMenuClient.BULLY_CATEGORY));
		net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (boostKey.consumeClick()) {
				if (client.player != null && net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.canSend(net.kraftattacke.bully.enchantment.ElytraBoostPayload.TYPE)) {
					net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(new net.kraftattacke.bully.enchantment.ElytraBoostPayload());
				}
			}
		});

		ItemTooltipCallback.EVENT.register((stack, context, flag, lines) -> {
			addSharpnessDamage(stack, lines);
			SwordCoating coating = stack.get(SwordCoating.TYPE);
			if (coating != null && net.kraftattacke.bully.config.BullyConfig.get().features.swordCoating) {
				lines.add(Component.translatable("item.bully.sword_coating", coating.hits()).withStyle(ChatFormatting.DARK_PURPLE));
				PotionContents.addPotionTooltip(coating.contents().getAllEffects(), lines::add, SwordCoating.DURATION_SCALE, context.tickRate());
			}
		});
	}

	private static final int DAMAGE_YELLOW = 0xCD8D0D;
	private static final Pattern NUMBER = Pattern.compile("\\d+(?:[.,]\\d+)?");

	/** Attack Damage line turns yellow and includes Sharpness; with Shift it keeps the base value and lists the bonus below. */
	private static void addSharpnessDamage(ItemStack stack, List<Component> lines) {
		var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(stack);
		int level = 0;
		for (Holder<Enchantment> enchantment : enchantments.keySet()) {
			if (enchantment.is(Enchantments.SHARPNESS)) {
				level = enchantments.getLevel(enchantment);
			}
		}
		if (level <= 0) {
			return;
		}
		Component damageName = Component.translatable("attribute.name.attack_damage");
		for (int i = 0; i < lines.size(); i++) {
			String text = lines.get(i).getString();
			Matcher number = NUMBER.matcher(text);
			if (!text.contains(damageName.getString()) || !number.find()) {
				continue;
			}
			double bonus = 1.0 + 0.5 * (level - 1); // vanilla Sharpness: +1 for level 1, +0.5 per level after
			String bonusText = format(bonus);
			if (Minecraft.getInstance().hasShiftDown()) {
				lines.add(i + 1, Component.literal(" +" + bonusText + " ").append(damageName)
						.withColor(DAMAGE_YELLOW)
						.append(Component.literal(" (").append(Component.translatable("enchantment.minecraft.sharpness")).append(")")
								.withColor(0x888888)));
			} else {
				double total = Double.parseDouble(number.group().replace(',', '.')) + bonus;
				lines.set(i, Component.literal(text.substring(0, number.start()) + format(total) + text.substring(number.end()))
						.withColor(DAMAGE_YELLOW)
						.append(Component.literal(" (for more details press Shift)").withColor(0x888888).withStyle(ChatFormatting.ITALIC)));
			}
			return;
		}
	}

	private static String format(double value) {
		return value == (long) value ? Long.toString((long) value) : String.format(Locale.ROOT, "%.1f", value);
	}
}
