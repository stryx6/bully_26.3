package net.kraftattacke.bully.item;

import java.util.function.Function;

import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.ScytheStats;
import net.kraftattacke.bully.ScytheStatsConfig;
import net.kraftattacke.bully.material.AetheraldToolMaterial;
import net.kraftattacke.bully.material.AuronyxToolMaterial;
import net.kraftattacke.bully.material.FeralythToolMaterial;
import net.kraftattacke.bully.material.FluxiteToolMaterial;
import net.kraftattacke.bully.material.MystraliteToolMaterial;
import net.kraftattacke.bully.material.NebulazulToolMaterial;
import net.kraftattacke.bully.material.VoidcristToolMaterial;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.ToolMaterial;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModTools {
	public static final Logger LOGGER = LoggerFactory.getLogger(ModTools.class);

	private static final ScytheStats SCYTHE_STATS = ScytheStatsConfig.load();

	public record MaterialToolSet(Item sword, Item pickaxe, Item axe, Item shovel, Item hoe, Item scythe, Item spear) {
	}

	public static final MaterialToolSet AETHERALD = registerMaterialSet(
			"aetherald",
			ModItems.AETHERALD_SWORD,
			AetheraldToolMaterial.INSTANCE,
			AetheraldToolMaterial.REPAIRS_AETHERALD_TOOLS
	);

	public static final MaterialToolSet AURONYX = registerMaterialSet(
			"auronyx",
			ModItems.AURONYX_SWORD,
			AuronyxToolMaterial.INSTANCE,
			AuronyxToolMaterial.REPAIRS_AURONYX_TOOLS
	);

	public static final MaterialToolSet FERALYTH = registerMaterialSet(
			"feralyth",
			ModItems.FERALYTH_SWORD,
			FeralythToolMaterial.INSTANCE,
			FeralythToolMaterial.REPAIRS_FERALYTH_TOOLS
	);

	public static final MaterialToolSet FLUXITE = registerMaterialSet(
			"fluxite",
			ModItems.FLUXITE_SWORD,
			FluxiteToolMaterial.INSTANCE,
			FluxiteToolMaterial.REPAIRS_FLUXITE_TOOLS
	);

	public static final MaterialToolSet MYSTRALITE = registerMaterialSet(
			"mystralite",
			ModItems.MYSTRALITE_SWORD,
			MystraliteToolMaterial.INSTANCE,
			MystraliteToolMaterial.REPAIRS_MYSTRALITE_TOOLS
	);

	public static final MaterialToolSet NEBULAZUL = registerMaterialSet(
			"nebulazul",
			ModItems.NEBULAZUL_SWORD,
			NebulazulToolMaterial.INSTANCE,
			NebulazulToolMaterial.REPAIRS_NEBULAZUL_TOOLS
	);

	public static final MaterialToolSet VOIDCRIST = registerMaterialSet(
			"voidcrist",
			ModItems.VOIDCRIST_SWORD,
			VoidcristToolMaterial.INSTANCE,
			VoidcristToolMaterial.REPAIRS_VOIDCRIST_TOOLS
	);

	private ModTools() {
	}

	public static void register() {
		LOGGER.info("Registering weapon and tool variants for {}", Bully.MOD_ID);
	}

	private static MaterialToolSet registerMaterialSet(String name, Item sword, ToolMaterial material, TagKey<Item> repairTag) {
		return new MaterialToolSet(
				sword,
				register(name + "_pickaxe", Item::new, new Item.Properties()
						.pickaxe(material, 1.0F, -2.8F)
						.rarity(Rarity.EPIC)
						.fireResistant()),
				register(name + "_axe", Item::new, new Item.Properties()
						.axe(material, 6.0F, -3.0F)
						.rarity(Rarity.EPIC)
						.fireResistant()),
				register(name + "_shovel", Item::new, new Item.Properties()
						.shovel(material, 1.5F, -3.0F)
						.rarity(Rarity.EPIC)
						.fireResistant()),
				register(name + "_hoe", Item::new, new Item.Properties()
						.hoe(material, -4.0F, 0.0F)
						.rarity(Rarity.EPIC)
						.fireResistant()),
				register(name + "_scythe", ScytheItem::new, new Item.Properties()
						.rarity(Rarity.EPIC)
						.durability(SCYTHE_STATS.durability())
						.component(DataComponents.TOOL, MaceItem.createToolProperties())
						.component(DataComponents.WEAPON, new Weapon(1))
						.attributes(createScytheAttributes(SCYTHE_STATS.attackDamage(), -2.8))
						.repairable(repairTag)
						.fireResistant()),
				register(name + "_spear", Item::new, new Item.Properties()
						.spear(material, 1.15F, 1.20F, 0.40F, 2.50F, 7.00F, 5.50F, 5.10F, 8.75F, 4.60F)
						.rarity(Rarity.EPIC)
						.fireResistant())
		);
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		Identifier id = Identifier.fromNamespaceAndPath(Bully.MOD_ID, name);
		Item.Properties propertiesWithId = properties.setId(ResourceKey.create(Registries.ITEM, id));
		T item = factory.apply(propertiesWithId);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	private static ItemAttributeModifiers createScytheAttributes(double attackDamage, double attackSpeed) {
		return ItemAttributeModifiers.builder()
				.add(
						Attributes.ATTACK_DAMAGE,
						new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, attackDamage, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.add(
						Attributes.ATTACK_SPEED,
						new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, attackSpeed, AttributeModifier.Operation.ADD_VALUE),
						EquipmentSlotGroup.MAINHAND
				)
				.build();
	}
}
