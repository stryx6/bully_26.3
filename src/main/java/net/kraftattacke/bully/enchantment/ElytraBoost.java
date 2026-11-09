package net.kraftattacke.bully.enchantment;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kraftattacke.bully.config.BullyConfig;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;

/**
 * Elytra Boost: with the enchanted elytra worn and gliding, the keybind fires a free firework boost
 * (flight duration = enchantment level) on a per-level cooldown.
 */
public final class ElytraBoost {
	private static final Map<UUID, Long> READY_AT = new ConcurrentHashMap<>();

	private ElytraBoost() {
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(ElytraBoostPayload.TYPE, ElytraBoostPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(ElytraBoostPayload.TYPE, (payload, context) -> boost(context.player()));

		// Treasure enchantment: only found in a few rare chests, never traded or from the enchanting table.
		LootTableEvents.MODIFY.register((key, builder, source, registries) -> {
			float chance;
			if (key.equals(BuiltInLootTables.END_CITY_TREASURE)) {
				chance = 0.30F;
			} else if (key.equals(BuiltInLootTables.ANCIENT_CITY)) {
				chance = 0.10F;
			} else {
				return;
			}
			var holder = registries.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
					.getOrThrow(ModEnchantments.ELYTRA_BOOST_KEY);
			builder.withPool(LootPool.lootPool()
					.when(LootItemRandomChanceCondition.randomChance(chance))
					.add(LootItem.lootTableItem(Items.ENCHANTED_BOOK)
							.apply(new EnchantRandomlyFunction.Builder().withEnchantment(holder))));
		});
	}

	private static void boost(ServerPlayer player) {
		if (!BullyConfig.get().features.elytraBoost || !player.isFallFlying()) {
			return;
		}
		var holder = player.level().registryAccess()
				.lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT)
				.getOrThrow(ModEnchantments.ELYTRA_BOOST_KEY);
		int level = EnchantmentHelper.getItemEnchantmentLevel(holder, player.getItemBySlot(EquipmentSlot.CHEST));
		if (level <= 0) {
			return;
		}
		long now = player.level().getGameTime();
		long readyAt = READY_AT.getOrDefault(player.getUUID(), 0L);
		if (now < readyAt) {
			player.sendSystemMessage(Component.translatable("message.bully.elytra_boost_cooldown", (readyAt - now + 19) / 20), true);
			return;
		}
		BullyConfig.ElytraBoost cfg = BullyConfig.get().elytraBoost;
		int seconds = level == 1 ? cfg.cooldownSecondsLevel1 : level == 2 ? cfg.cooldownSecondsLevel2 : cfg.cooldownSecondsLevel3;
		READY_AT.put(player.getUUID(), now + seconds * 20L);

		ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
		rocket.set(DataComponents.FIREWORKS, new Fireworks(Math.min(level, 3), List.of()));
		player.level().addFreshEntity(new FireworkRocketEntity(player.level(), rocket, player));
	}
}
