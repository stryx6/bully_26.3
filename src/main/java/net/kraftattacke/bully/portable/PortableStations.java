package net.kraftattacke.bully.portable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.kraftattacke.bully.config.BullyConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Opens vanilla workbench menus from carried blocks (no placed block needed), and cycles between carried ones. */
public final class PortableStations {
	/** Vanilla anvil degrade chance per use at a placed anvil. */
	private static final float VANILLA_ANVIL_DEGRADE_CHANCE = 0.12F;

	@FunctionalInterface
	private interface Factory {
		AbstractContainerMenu create(int id, Inventory inventory, int listPos, int slot);
	}

	/** One workbench kind. The same Entry instance is shared by all items that open it (e.g. the three anvil tiers). */
	private record Entry(Factory factory, String titleKey) { }

	/** A resolved station the player is carrying: which workbench, and the representative inventory slot. */
	private record Station(Entry entry, int slot) { }

	private static final Entry ANVIL = new Entry(PortableAnvilMenu::new, "container.repair");

	private static final Map<Item, Entry> ITEMS = Map.ofEntries(
			Map.entry(Items.CRAFTING_TABLE, new Entry(PortableCraftingMenu::new, "container.crafting")),
			Map.entry(Items.ANVIL, ANVIL),
			Map.entry(Items.CHIPPED_ANVIL, ANVIL),
			Map.entry(Items.DAMAGED_ANVIL, ANVIL),
			Map.entry(Items.SMITHING_TABLE, new Entry(PortableSmithingMenu::new, "container.upgrade")),
			Map.entry(Items.GRINDSTONE, new Entry(PortableGrindstoneMenu::new, "container.grindstone_title")),
			Map.entry(Items.STONECUTTER, new Entry(PortableStonecutterMenu::new, "container.stonecutter")),
			Map.entry(Items.LOOM, new Entry(PortableLoomMenu::new, "container.loom")),
			Map.entry(Items.CARTOGRAPHY_TABLE, new Entry(PortableCartographyMenu::new, "container.cartography_table")));

	private PortableStations() { }

	/** Real level access so recipes compute; the menus override stillValid to stay open without a block. */
	public static ContainerLevelAccess access(Inventory inventory) {
		return ContainerLevelAccess.create(inventory.player.level(), inventory.player.blockPosition());
	}

	/** One entry per distinct workbench kind, in first-appearance slot order. Anvils collapse to the best tier. */
	private static List<Station> scan(Inventory inventory) {
		LinkedHashMap<Entry, Integer> found = new LinkedHashMap<>();
		for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
			Entry entry = ITEMS.get(inventory.getItem(slot).getItem());
			if (entry == null) continue;
			Integer existing = found.get(entry);
			if (existing == null) {
				found.put(entry, slot); // first appearance fixes the cycle order
			} else if (entry == ANVIL
					&& anvilTier(inventory.getItem(slot)) < anvilTier(inventory.getItem(existing))) {
				found.put(entry, slot); // keep the least-damaged anvil as the one to open/wear
			}
		}
		List<Station> stations = new ArrayList<>();
		found.forEach((entry, slot) -> stations.add(new Station(entry, slot)));
		return stations;
	}

	/** 0 = pristine anvil, 1 = chipped, 2 = damaged, 3 = not an anvil. Lower is better. */
	private static int anvilTier(ItemStack stack) {
		Item item = stack.getItem();
		if (item == Items.ANVIL) return 0;
		if (item == Items.CHIPPED_ANVIL) return 1;
		if (item == Items.DAMAGED_ANVIL) return 2;
		return 3;
	}

	private static int cycle(int listPos, int size, int direction) {
		return Math.floorMod(listPos + direction, size);
	}

	public static void register() {
		PayloadTypeRegistry.serverboundPlay().register(OpenPortablePayload.TYPE, OpenPortablePayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CyclePortablePayload.TYPE, CyclePortablePayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(PortableInfoPayload.TYPE, PortableInfoPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(OpenPortablePayload.TYPE, (payload, context) -> {
			if (!BullyConfig.get().utilityMenuEnabled) return;
			open(context.player(), 0);
		});
		ServerPlayNetworking.registerGlobalReceiver(CyclePortablePayload.TYPE, (payload, context) -> {
			if (!BullyConfig.get().utilityMenuEnabled) return;
			ServerPlayer player = context.player();
			if (player.containerMenu instanceof PortableStation current) {
				List<Station> stations = scan(player.getInventory());
				if (!stations.isEmpty()) {
					open(player, cycle(current.bully$index(), stations.size(), payload.direction()));
				}
			}
		});
	}

	/** Opens the carried workbench at the given list position (wrapped); does nothing if none are carried. */
	public static void open(ServerPlayer player, int listPos) {
		List<Station> stations = scan(player.getInventory());
		if (stations.isEmpty()) return;
		int size = stations.size();
		int pos = Math.floorMod(listPos, size);
		Station station = stations.get(pos);
		Component title = Component.translatable(station.entry().titleKey());
		player.openMenu(new SimpleMenuProvider(
				(id, inventory, p) -> station.entry().factory().create(id, inventory, pos, station.slot()), title));
		// Tell the client which inventory slots the current / left / right stations live in, for the highlights.
		int prev = stations.get(cycle(pos, size, -1)).slot();
		int next = stations.get(cycle(pos, size, 1)).slot();
		ServerPlayNetworking.send(player, new PortableInfoPayload(station.slot(), prev, next));
	}

	/** Wears down the carried anvil item (Anvil -> Chipped -> Damaged -> gone), faster than a placed one. */
	public static void degradeCarriedAnvil(Player player, int slot) {
		if (player.hasInfiniteMaterials()) return;
		float fraction = BullyConfig.get().portable.anvilDegradeUseFraction;
		if (fraction <= 0.0F) return;
		float chance = VANILLA_ANVIL_DEGRADE_CHANCE / fraction;
		if (player.getRandom().nextFloat() >= chance) return;

		ItemStack held = player.getInventory().getItem(slot);
		Item item = held.getItem();
		Item next;
		if (item == Items.ANVIL) next = Items.CHIPPED_ANVIL;
		else if (item == Items.CHIPPED_ANVIL) next = Items.DAMAGED_ANVIL;
		else if (item == Items.DAMAGED_ANVIL) next = null; // breaks
		else return; // slot no longer holds a portable anvil

		if (held.getCount() == 1) {
			player.getInventory().setItem(slot, next == null ? ItemStack.EMPTY : new ItemStack(next));
		} else {
			held.shrink(1);
			if (next != null) player.getInventory().add(new ItemStack(next));
		}
		player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
				next == null ? SoundEvents.ANVIL_DESTROY : SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.5F, 1.0F);
	}
}
