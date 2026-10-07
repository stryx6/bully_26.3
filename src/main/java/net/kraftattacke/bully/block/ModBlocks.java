package net.kraftattacke.bully.block;

import java.util.function.Function;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class ModBlocks {
	public static final Block ANTIMATTER_BLOCK = registerBlock(
			"antimatter_block",
			Block::new,
			BlockBehaviour.Properties.of().strength(4.0F).requiresCorrectToolForDrops().sound(SoundType.NETHERITE_BLOCK)
	);
	public static final Block ROSARIUM_CRYSTAL_BLOCK = registerBlock(
			"rosarium_crystal_block",
			Block::new,
			BlockBehaviour.Properties.of().strength(1.5F).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
	);

	private static <T extends Block> T registerBlock(
			String name,
			Function<BlockBehaviour.Properties, T> factory,
			BlockBehaviour.Properties properties
	) {
		Identifier id = Identifier.fromNamespaceAndPath(Bully.MOD_ID, name);
		// 26.2 requires block properties to have their id set before Block construction.
		BlockBehaviour.Properties propertiesWithId = properties.setId(ResourceKey.create(Registries.BLOCK, id));
		T block = factory.apply(propertiesWithId);
		Registry.register(BuiltInRegistries.BLOCK, id, block);
		registerBlockItem(name, block);
		return block;
	}

	private static void registerBlockItem(String name, Block block) {
		Identifier id = Identifier.fromNamespaceAndPath(Bully.MOD_ID, name);
		Item.Properties properties = new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id));
		Registry.register(BuiltInRegistries.ITEM, id, new BlockItem(block, properties));
	}

	public static void registerModBlocks() {
		Bully.LOGGER.info("Registering Mod Blocks for {}", Bully.MOD_ID);

	}
}
