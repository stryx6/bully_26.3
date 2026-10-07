package net.kraftattacke.bully.material;

import net.kraftattacke.bully.Bully;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorMaterials;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;

public final class AntimatterArmorMaterial {
	private AntimatterArmorMaterial() {
	}

	public static final int BASE_DURABILITY = 100;

	public static final TagKey<Item> REPAIRS_ANTIMATTER_ARMOR = TagKey.create(
			BuiltInRegistries.ITEM.key(),
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "repairs_antimatter_armor")
	);

	private static ResourceKey<EquipmentAsset> assetId(String path) {
		return ResourceKey.create(EquipmentAssets.ROOT_ID, Identifier.fromNamespaceAndPath(Bully.MOD_ID, path));
	}

	public static final ArmorMaterial INSTANCE = new ArmorMaterial(
			BASE_DURABILITY,
			ArmorMaterials.makeDefense(6, 8, 10, 6, 60),
			5,
			SoundEvents.ARMOR_EQUIP_NETHERITE,
			4.0F,
			0.12F,
			REPAIRS_ANTIMATTER_ARMOR,
			assetId("antimatter")
	);
}
