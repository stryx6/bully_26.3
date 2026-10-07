package net.kraftattacke.bully.material;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;

public final class MystraliteToolMaterial {
	private MystraliteToolMaterial() {
	}

	public static final TagKey<Item> REPAIRS_MYSTRALITE_TOOLS = TagKey.create(
			BuiltInRegistries.ITEM.key(),
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "repairs_mystralite_tools")
	);

	public static final ToolMaterial INSTANCE = new ToolMaterial(
			BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
			4096,
			15.0F,
			5.0F,
			25,
			REPAIRS_MYSTRALITE_TOOLS
	);
}
