package net.kraftattacke.bully.villager;

import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.fabricmc.fabric.api.object.builder.v1.world.poi.PoiHelper;
import net.kraftattacke.bully.Bully;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.item.trading.TradeSet;
import net.minecraft.world.level.block.Blocks;

public final class ModVillagers {
	private static final Identifier DISCOGRAPHER_ID = Identifier.fromNamespaceAndPath(Bully.MOD_ID, "discographer");
	private static final ResourceKey<PoiType> DISCOGRAPHER_POI = ResourceKey.create(Registries.POINT_OF_INTEREST_TYPE, DISCOGRAPHER_ID);
	public static final ResourceKey<VillagerProfession> DISCOGRAPHER_PROFESSION = ResourceKey.create(Registries.VILLAGER_PROFESSION, DISCOGRAPHER_ID);

	private ModVillagers() {
	}

	public static void register() {
		Bully.LOGGER.info("Registering villagers for {}", Bully.MOD_ID);

		PoiHelper.register(DISCOGRAPHER_ID, 1, 1, Blocks.JUKEBOX);

		Registry.register(
				BuiltInRegistries.VILLAGER_PROFESSION,
				DISCOGRAPHER_PROFESSION,
				new VillagerProfession(
						Component.literal("Discographer"),
						holder -> holder.is(DISCOGRAPHER_POI),
						holder -> holder.is(DISCOGRAPHER_POI),
						ImmutableSet.of(),
						ImmutableSet.of(),
						SoundEvents.VILLAGER_WORK_LIBRARIAN,
						Int2ObjectMap.ofEntries(
								Int2ObjectMap.entry(1, tradeSet("trade_set/discographer/level_1")),
								Int2ObjectMap.entry(2, tradeSet("trade_set/discographer/level_2")),
								Int2ObjectMap.entry(3, tradeSet("trade_set/discographer/level_3")),
								Int2ObjectMap.entry(4, tradeSet("trade_set/discographer/level_4")),
								Int2ObjectMap.entry(5, tradeSet("trade_set/discographer/level_5"))
						)
				)
		);
	}

	private static ResourceKey<TradeSet> tradeSet(String path) {
		return ResourceKey.create(Registries.TRADE_SET, Identifier.fromNamespaceAndPath(Bully.MOD_ID, path));
	}
}
