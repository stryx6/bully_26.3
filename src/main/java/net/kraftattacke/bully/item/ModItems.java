package net.kraftattacke.bully.item;

import java.util.List;
import java.util.function.Function;

import net.kraftattacke.bully.material.AetheraldToolMaterial;
import net.kraftattacke.bully.material.AetheraldArmorMaterial;
import net.kraftattacke.bully.material.FluxiteArmorMaterial;
import net.kraftattacke.bully.material.FluxiteToolMaterial;
import net.kraftattacke.bully.material.AntimatterArmorMaterial;
import net.kraftattacke.bully.material.AntimatterToolMaterial;
import net.kraftattacke.bully.material.AuronyxArmorMaterial;
import net.kraftattacke.bully.material.AuronyxToolMaterial;
import net.kraftattacke.bully.material.FeralythArmorMaterial;
import net.kraftattacke.bully.material.FeralythToolMaterial;
import net.kraftattacke.bully.material.MystraliteArmorMaterial;
import net.kraftattacke.bully.material.MystraliteToolMaterial;
import net.kraftattacke.bully.material.NebulazulArmorMaterial;
import net.kraftattacke.bully.material.NebulazulToolMaterial;
import net.kraftattacke.bully.material.VoidcristArmorMaterial;
import net.kraftattacke.bully.material.VoidcristToolMaterial;
import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.ScytheStats;
import net.kraftattacke.bully.ScytheStatsConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.MaceItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SmithingTemplateItem;
import net.minecraft.world.item.component.AttackRange;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.item.equipment.EquipmentAssets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ModItems {
	public static final Logger LOGGER = LoggerFactory.getLogger(ModItems.class);

	private static final ScytheStats SCYTHE_STATS = ScytheStatsConfig.load();
	private static final ResourceKey<JukeboxSong> ONRYO_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "onryo")
	);

	private static final ResourceKey<JukeboxSong> UNDERTALE_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "undertale")
	);

	private static final ResourceKey<JukeboxSong> ATTACK_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "attack")
	);

	private static final ResourceKey<JukeboxSong> EVERMERE_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "evermere")
	);

	private static final ResourceKey<JukeboxSong> ELDERWOOD_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "elderwood")
	);

	private static final ResourceKey<JukeboxSong> FORGOTTEN_TALE_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "forgotten_tale")
	);

	private static final ResourceKey<JukeboxSong> SHADOWROOT_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "shadowroot")
	);

	private static final ResourceKey<JukeboxSong> SILVERROAD_JUKEBOX_SONG = ResourceKey.create(
			Registries.JUKEBOX_SONG,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "silverroad")
	);


	public static final Item ANTIMATTER = register(
			"antimatter",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item ROSARIUM_CRYSTAL = register(
			"rosarium_crystal", Item::new,
			new Item.Properties().trimMaterial(ResourceKey.create(
					Registries.TRIM_MATERIAL, Identifier.fromNamespaceAndPath(Bully.MOD_ID, "rosarium")))
	);

	public static final Item ECHO_ARMOR_TRIM_SMITHING_TEMPLATE = registerTrimTemplate("echo");
	public static final Item RIFT_ARMOR_TRIM_SMITHING_TEMPLATE = registerTrimTemplate("rift");
	public static final Item CROWN_ARMOR_TRIM_SMITHING_TEMPLATE = registerTrimTemplate("crown");
	public static final Item FANG_ARMOR_TRIM_SMITHING_TEMPLATE = registerTrimTemplate("fang");
	public static final Item THORN_ARMOR_TRIM_SMITHING_TEMPLATE = registerTrimTemplate("thorn");

	public static final Item AETHERALD = register(
			"aetherald",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item AURONYX = register(
			"auronyx",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item FERALYTH = register(
			"feralyth",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item FLUXITE = register(
			"fluxite",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item MYSTRALITE = register(
			"mystralite",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item NEBULAZUL = register(
			"nebulazul",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item VOIDCRIST = register(
			"voidcrist",
			Item::new,
			new Item.Properties()
					.fireResistant()
	);

	public static final Item LARGE_STICK = register(
			"large_stick",
			Item::new,
			new Item.Properties()
					// Half a block more reach than a spear: spear maxReach 4.5 -> 5.0 (creative 6.5 -> 7.0).
					.component(DataComponents.ATTACK_RANGE, new AttackRange(2.0F, 5.0F, 2.0F, 7.0F, 0.125F, 0.5F))
					// The hit indicator only shows when the cooldown is > 5 ticks; the default speed 4.0 is exactly 5. Speed 3.8 (5.3 ticks).
					.attributes(ItemAttributeModifiers.builder()
							.add(
									Attributes.ATTACK_SPEED,
									new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -0.2, AttributeModifier.Operation.ADD_VALUE),
									EquipmentSlotGroup.MAINHAND
							)
							.build())
	);

	public static final Item MAGIC_CRYSTAL = register(
			"magic_crystal",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.rarity(Rarity.RARE)
					.fireResistant())
	);

	public static final Item DARK_STAR = register(
			"dark_star",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_UPGRADE_SMITHING_TEMPLATE = register(
			"antimatter_upgrade_smithing_template",
			props -> new SmithingTemplateItem(
					Component.translatable("item.bully.antimatter_upgrade_smithing_template.applies_to"),
					Component.translatable("item.bully.antimatter_upgrade_smithing_template.ingredients"),
					Component.translatable("item.bully.antimatter_upgrade_smithing_template"),
					Component.translatable("item.bully.antimatter_upgrade_smithing_template.base_slot_description"),
					// Base: armor + tools
					List.of(
							Identifier.withDefaultNamespace("container/slot/helmet"),
							Identifier.withDefaultNamespace("container/slot/chestplate"),
							Identifier.withDefaultNamespace("container/slot/leggings"),
							Identifier.withDefaultNamespace("container/slot/boots"),
							Identifier.withDefaultNamespace("container/slot/sword"),
							Identifier.withDefaultNamespace("container/slot/pickaxe"),
							Identifier.withDefaultNamespace("container/slot/axe"),
							Identifier.withDefaultNamespace("container/slot/shovel"),
							Identifier.withDefaultNamespace("container/slot/hoe")
					),
					// Addition: antimatter (treat like an ingot)
					List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
					props
			),
			new Item.Properties()
	);

	public static final Item ARMOR_UPGRADE_SMITHING_TEMPLATE = register(
			"armor_upgrade_smithing_template",
			props -> new SmithingTemplateItem(
					Component.translatable("item.bully.armor_upgrade_smithing_template.applies_to"),
					Component.translatable("item.bully.armor_upgrade_smithing_template.ingredients"),
					Component.translatable("item.bully.armor_upgrade_smithing_template"),
					Component.translatable("item.bully.armor_upgrade_smithing_template.base_slot_description"),
					// Base: Armor
					List.of(
							Identifier.withDefaultNamespace("container/slot/helmet"),
							Identifier.withDefaultNamespace("container/slot/chestplate"),
							Identifier.withDefaultNamespace("container/slot/leggings"),
							Identifier.withDefaultNamespace("container/slot/boots")
					),
					// Addition: Antimatter or Ore Blocks(treat like an ingot)
					List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
					props
			),
			new Item.Properties()
	);

	public static final Item TOOL_UPGRADE_SMITHING_TEMPLATE = register(
			"tool_upgrade_smithing_template",
			props -> new SmithingTemplateItem(
					Component.translatable("item.bully.tool_upgrade_smithing_template.applies_to"),
					Component.translatable("item.bully.tool_upgrade_smithing_template.ingredients"),
					Component.translatable("item.bully.tool_upgrade_smithing_template"),
					Component.translatable("item.bully.tool_upgrade_smithing_template.base_slot_description"),
					// Base: Tools + Weapons
					List.of(
							Identifier.withDefaultNamespace("container/slot/sword"),
							Identifier.withDefaultNamespace("container/slot/pickaxe"),
							Identifier.withDefaultNamespace("container/slot/axe"),
							Identifier.withDefaultNamespace("container/slot/shovel"),
							Identifier.withDefaultNamespace("container/slot/hoe"),
							Identifier.withDefaultNamespace("container/slot/scythe")
					),
					// Addition: Antimatter or Ore Blocks
					List.of(Identifier.withDefaultNamespace("container/slot/ingot")),
					props
			),
			new Item.Properties()
	);

	public static final Item ANTIMATTER_SWORD = register(
			"antimatter_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(AntimatterToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AETHERALD_SWORD = register(
			"aetherald_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(AetheraldToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AURONYX_SWORD = register(
			"auronyx_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(AuronyxToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FERALYTH_SWORD = register(
			"feralyth_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(FeralythToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FLUXITE_SWORD = register(
			"fluxite_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(FluxiteToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item MYSTRALITE_SWORD = register(
			"mystralite_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(MystraliteToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item NEBULAZUL_SWORD = register(
			"nebulazul_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(NebulazulToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item VOIDCRIST_SWORD = register(
			"voidcrist_sword",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.sword(VoidcristToolMaterial.INSTANCE, 4.0F, -2.2F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_PICKAXE = register(
			"antimatter_pickaxe",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.pickaxe(AntimatterToolMaterial.INSTANCE, 1.0F, -2.8F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_AXE = register(
			"antimatter_axe",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.axe(AntimatterToolMaterial.INSTANCE, 6.0F, -3.0F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_SHOVEL = register(
			"antimatter_shovel",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.shovel(AntimatterToolMaterial.INSTANCE, 1.5F, -3.0F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_HOE = register(
			"antimatter_hoe",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.hoe(AntimatterToolMaterial.INSTANCE, -4.0F, 0.0F)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_SCYTHE = register(
			"antimatter_scythe",
			ScytheItem::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.rarity(Rarity.EPIC)
					.durability(SCYTHE_STATS.durability())
					.component(DataComponents.TOOL, MaceItem.createToolProperties())
					.component(DataComponents.WEAPON, new Weapon(1))
					.attributes(createScytheAttributes(SCYTHE_STATS.attackDamage(), -2.8))
					.repairable(AntimatterToolMaterial.REPAIRS_ANTIMATTER_TOOLS)
					.fireResistant())
	);

	public static final Item ANTIMATTER_SPEAR = register(
			"antimatter_spear",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					// Match vanilla spear tuning; ToolMaterial controls durability/repair.
					.spear(
							AntimatterToolMaterial.INSTANCE,
							1.15F, 1.20F, 0.40F,
							2.50F, 7.00F, 5.50F,
							5.10F, 8.75F, 4.60F
					)
					.rarity(Rarity.EPIC)
					.attributes(createScytheAttributes(6.0, 1.0 / 1.15 - 4.0))
					.fireResistant())
	);

	public static final Item MAGIC_WAND = register(
			"magic_wand",
			MagicWandItem::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.stacksTo(1)
					.durability(1250)
					.enchantable(18)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_ELYTRA = register(
			"antimatter_elytra",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					// Vanilla elytra: 432. Boost this significantly.
					.durability(1296)
					.rarity(Rarity.EPIC)
					.component(DataComponents.GLIDER, Unit.INSTANCE)
					.component(
							DataComponents.EQUIPPABLE,
							Equippable.builder(EquipmentSlot.CHEST)
									.setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA)
									.setAsset(EquipmentAssets.ELYTRA)
									.setDamageOnHurt(false)
									.build()
					)
					.repairable(ANTIMATTER)
					.fireResistant())
	);

	public static final Item ANTIMATTER_HELMET = register(
			"antimatter_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AntimatterArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AETHERALD_HELMET = register(
			"aetherald_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AetheraldArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FLUXITE_HELMET = register(
			"fluxite_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FluxiteArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item NEBULAZUL_HELMET = register(
			"nebulazul_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(NebulazulArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item MYSTRALITE_HELMET = register(
			"mystralite_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(MystraliteArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item VOIDCRIST_HELMET = register(
			"voidcrist_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(VoidcristArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AURONYX_HELMET = register(
			"auronyx_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AuronyxArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FERALYTH_HELMET = register(
			"feralyth_helmet",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FeralythArmorMaterial.INSTANCE, ArmorType.HELMET)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_CHESTPLATE = register(
			"antimatter_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AntimatterArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AETHERALD_CHESTPLATE = register(
			"aetherald_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AetheraldArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FLUXITE_CHESTPLATE = register(
			"fluxite_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FluxiteArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item NEBULAZUL_CHESTPLATE = register(
			"nebulazul_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(NebulazulArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item MYSTRALITE_CHESTPLATE = register(
			"mystralite_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(MystraliteArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item VOIDCRIST_CHESTPLATE = register(
			"voidcrist_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(VoidcristArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AURONYX_CHESTPLATE = register(
			"auronyx_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AuronyxArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FERALYTH_CHESTPLATE = register(
			"feralyth_chestplate",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FeralythArmorMaterial.INSTANCE, ArmorType.CHESTPLATE)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_LEGGINGS = register(
			"antimatter_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AntimatterArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AETHERALD_LEGGINGS = register(
			"aetherald_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AetheraldArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FLUXITE_LEGGINGS = register(
			"fluxite_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FluxiteArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item NEBULAZUL_LEGGINGS = register(
			"nebulazul_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(NebulazulArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item MYSTRALITE_LEGGINGS = register(
			"mystralite_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(MystraliteArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item VOIDCRIST_LEGGINGS = register(
			"voidcrist_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(VoidcristArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);
	public static final Item AURONYX_LEGGINGS = register(
			"auronyx_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AuronyxArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);
	public static final Item FERALYTH_LEGGINGS = register(
			"feralyth_leggings",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FeralythArmorMaterial.INSTANCE, ArmorType.LEGGINGS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item ANTIMATTER_BOOTS = register(
			"antimatter_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AntimatterArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AETHERALD_BOOTS = register(
			"aetherald_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AetheraldArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FLUXITE_BOOTS = register(
			"fluxite_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FluxiteArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item NEBULAZUL_BOOTS = register(
			"nebulazul_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(NebulazulArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item MYSTRALITE_BOOTS = register(
			"mystralite_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(MystraliteArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item VOIDCRIST_BOOTS = register(
			"voidcrist_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(VoidcristArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item AURONYX_BOOTS = register(
			"auronyx_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(AuronyxArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item FERALYTH_BOOTS = register(
			"feralyth_boots",
			Item::new,
			ExplosionResistantItems.protect(new Item.Properties()
					.humanoidArmor(FeralythArmorMaterial.INSTANCE, ArmorType.BOOTS)
					.rarity(Rarity.EPIC)
					.fireResistant())
	);

	public static final Item MUSIC_DISC_ONRYO = register(
			"music_disc_onryo",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(ONRYO_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_ATTACK = register(
			"music_disc_attack",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(ATTACK_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_UNDERTALE = register(
			"music_disc_undertale",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(UNDERTALE_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_EVERMERE = register(
			"music_disc_evermere",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(EVERMERE_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_ELDERWOOD = register(
			"music_disc_elderwood",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(ELDERWOOD_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_FORGOTTEN_TALE = register(
			"music_disc_forgotten_tale",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(FORGOTTEN_TALE_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_SILVERROAD = register(
			"music_disc_silverroad",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(SILVERROAD_JUKEBOX_SONG)
	);

	public static final Item MUSIC_DISC_SHADOWROOT = register(
			"music_disc_shadowroot",
			Item::new,
			new Item.Properties()
					.stacksTo(1)
					.rarity(Rarity.RARE)
					.jukeboxPlayable(SHADOWROOT_JUKEBOX_SONG)
	);

	public static final  Item EMPTY_TRIM_TEMPLATE = register(
			"empty_trim_template",
			Item::new,
			new Item.Properties()
					.rarity(Rarity.UNCOMMON)

	);

	private ModItems() {
	}

	private static Item registerTrimTemplate(String pattern) {
		return register(pattern + "_armor_trim_smithing_template",
				SmithingTemplateItem::createArmorTrimTemplate, new Item.Properties());
	}

	public static void register() {
		LOGGER.info("Registering items for {}", Bully.MOD_ID);
		// Static initializers do the actual registrations.
	}

	private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties properties) {
		Identifier id = Identifier.fromNamespaceAndPath(Bully.MOD_ID, name);
		// 26.2 requires item properties to have their id set before Item construction.
		Item.Properties propertiesWithId = properties.setId(ResourceKey.create(Registries.ITEM, id));
		T item = factory.apply(propertiesWithId);
		return Registry.register(BuiltInRegistries.ITEM, id, item);
	}

	private static ItemAttributeModifiers createScytheAttributes(double attackDamage, double attackSpeed) {
		// Mirrors vanilla MaceItem.createAttributes() but values come from config.
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

	public static void registerItemGroups() {
		ModCreativeTabs.register();
	}
}
