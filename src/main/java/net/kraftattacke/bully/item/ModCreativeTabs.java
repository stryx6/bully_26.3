package net.kraftattacke.bully.item;

import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.block.ModBlocks;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTabs {
	public static final CreativeModeTab BULLY_ITEMS = Registry.register(
			BuiltInRegistries.CREATIVE_MODE_TAB,
			Identifier.fromNamespaceAndPath(Bully.MOD_ID, "bully_items"),
			CreativeModeTab.builder(CreativeModeTab.Row.BOTTOM, 0)
					.title(Component.translatable("itemGroup.bully.bully_items"))
					.icon(() -> new ItemStack(ModItems.ANTIMATTER))
					.displayItems((parameters, output) -> {
						addAntimatterSection(output);
						addTier(output, ModItems.AETHERALD, ModTools.AETHERALD,
								ModItems.AETHERALD_HELMET,
								ModItems.AETHERALD_CHESTPLATE,
								ModItems.AETHERALD_LEGGINGS,
								ModItems.AETHERALD_BOOTS);
						addTier(output, ModItems.AURONYX, ModTools.AURONYX,
								ModItems.AURONYX_HELMET,
								ModItems.AURONYX_CHESTPLATE,
								ModItems.AURONYX_LEGGINGS,
								ModItems.AURONYX_BOOTS);
						addTier(output, ModItems.FERALYTH, ModTools.FERALYTH,
								ModItems.FERALYTH_HELMET,
								ModItems.FERALYTH_CHESTPLATE,
								ModItems.FERALYTH_LEGGINGS,
								ModItems.FERALYTH_BOOTS);
						addTier(output, ModItems.FLUXITE, ModTools.FLUXITE,
								ModItems.FLUXITE_HELMET,
								ModItems.FLUXITE_CHESTPLATE,
								ModItems.FLUXITE_LEGGINGS,
								ModItems.FLUXITE_BOOTS);
						addTier(output, ModItems.MYSTRALITE, ModTools.MYSTRALITE,
								ModItems.MYSTRALITE_HELMET,
								ModItems.MYSTRALITE_CHESTPLATE,
								ModItems.MYSTRALITE_LEGGINGS,
								ModItems.MYSTRALITE_BOOTS);
						addTier(output, ModItems.NEBULAZUL, ModTools.NEBULAZUL,
								ModItems.NEBULAZUL_HELMET,
								ModItems.NEBULAZUL_CHESTPLATE,
								ModItems.NEBULAZUL_LEGGINGS,
								ModItems.NEBULAZUL_BOOTS);
						addTier(output, ModItems.VOIDCRIST, ModTools.VOIDCRIST,
								ModItems.VOIDCRIST_HELMET,
								ModItems.VOIDCRIST_CHESTPLATE,
								ModItems.VOIDCRIST_LEGGINGS,
								ModItems.VOIDCRIST_BOOTS);
						addEnchantBooksAndPotions(parameters, output);
						output.accept(ModItems.MUSIC_DISC_ATTACK);
						output.accept(ModItems.MUSIC_DISC_ELDERWOOD);
						output.accept(ModItems.MUSIC_DISC_EVERMERE);
						output.accept(ModItems.MUSIC_DISC_FORGOTTEN_TALE);
						output.accept(ModItems.MUSIC_DISC_ONRYO);
						output.accept(ModItems.MUSIC_DISC_SHADOWROOT);
						output.accept(ModItems.MUSIC_DISC_SILVERROAD);
						output.accept(ModItems.MUSIC_DISC_UNDERTALE);
					})
					.build()
	);

	private ModCreativeTabs() {
	}

	public static void register() {
		// Static initialization performs the registration.
	}

	private static void addEnchantBooksAndPotions(CreativeModeTab.ItemDisplayParameters parameters, CreativeModeTab.Output output) {
		var features = net.kraftattacke.bully.config.BullyConfig.get().features;
		var enchantments = parameters.holders().lookupOrThrow(net.minecraft.core.registries.Registries.ENCHANTMENT);
		record Book(net.minecraft.resources.ResourceKey<net.minecraft.world.item.enchantment.Enchantment> key, int maxLevel, boolean enabled) { }
		for (Book book : new Book[] {
				new Book(net.kraftattacke.bully.enchantment.ModEnchantments.MACE_GUARD_KEY, 4, features.maceGuard),
				new Book(net.kraftattacke.bully.enchantment.ModEnchantments.PULL_KEY, 1, features.pull),
				new Book(net.kraftattacke.bully.enchantment.ModEnchantments.HUNGER_LEECH_KEY, 3, features.hungerLeech),
				new Book(net.kraftattacke.bully.enchantment.ModEnchantments.ELYTRA_BOOST_KEY, 3, features.elytraBoost)}) {
			if (!book.enabled()) continue;
			var holder = enchantments.getOrThrow(book.key());
			for (int level = 1; level <= book.maxLevel(); level++) {
				output.accept(net.minecraft.world.item.enchantment.EnchantmentHelper.createBook(
						new net.minecraft.world.item.enchantment.EnchantmentInstance(holder, level)));
			}
		}
		if (features.grievousWoundsPotions) {
			for (var potion : java.util.List.of(net.kraftattacke.bully.effect.ModEffects.GRIEVOUS_WOUNDS_POTION,
					net.kraftattacke.bully.effect.ModEffects.LONG_GRIEVOUS_WOUNDS_POTION,
					net.kraftattacke.bully.effect.ModEffects.STRONG_GRIEVOUS_WOUNDS_POTION)) {
				for (Item item : new Item[] {net.minecraft.world.item.Items.POTION, net.minecraft.world.item.Items.SPLASH_POTION,
						net.minecraft.world.item.Items.LINGERING_POTION, net.minecraft.world.item.Items.TIPPED_ARROW}) {
					output.accept(net.minecraft.world.item.alchemy.PotionContents.createItemStack(item, potion));
				}
			}
		}
	}

	private static void addAntimatterSection(CreativeModeTab.Output output) {
		output.accept(ModItems.ANTIMATTER);
		output.accept(ModBlocks.ANTIMATTER_BLOCK);
		output.accept(ModItems.ROSARIUM_CRYSTAL);
		output.accept(ModBlocks.ROSARIUM_CRYSTAL_BLOCK);
		output.accept(ModItems.LARGE_STICK);
		output.accept(ModItems.DARK_STAR);
		output.accept(ModItems.MAGIC_CRYSTAL);
		output.accept(ModItems.EMPTY_TRIM_TEMPLATE);
		output.accept(ModItems.ECHO_ARMOR_TRIM_SMITHING_TEMPLATE);
		output.accept(ModItems.RIFT_ARMOR_TRIM_SMITHING_TEMPLATE);
		output.accept(ModItems.CROWN_ARMOR_TRIM_SMITHING_TEMPLATE);
		output.accept(ModItems.FANG_ARMOR_TRIM_SMITHING_TEMPLATE);
		output.accept(ModItems.THORN_ARMOR_TRIM_SMITHING_TEMPLATE);
		output.accept(ModItems.ANTIMATTER_UPGRADE_SMITHING_TEMPLATE);
		output.accept(ModItems.ARMOR_UPGRADE_SMITHING_TEMPLATE);
		output.accept(ModItems.TOOL_UPGRADE_SMITHING_TEMPLATE);
		if (net.kraftattacke.bully.config.BullyConfig.get().features.magicWand) output.accept(ModItems.MAGIC_WAND);
		output.accept(ModItems.ANTIMATTER_SWORD);
		output.accept(ModItems.ANTIMATTER_PICKAXE);
		output.accept(ModItems.ANTIMATTER_AXE);
		output.accept(ModItems.ANTIMATTER_SHOVEL);
		output.accept(ModItems.ANTIMATTER_HOE);
		if (net.kraftattacke.bully.config.BullyConfig.get().features.scythes) output.accept(ModItems.ANTIMATTER_SCYTHE);
		output.accept(ModItems.ANTIMATTER_SPEAR);

		output.accept(ModItems.ANTIMATTER_ELYTRA);
		output.accept(ModItems.ANTIMATTER_HELMET);
		output.accept(ModItems.ANTIMATTER_CHESTPLATE);
		output.accept(ModItems.ANTIMATTER_LEGGINGS);
		output.accept(ModItems.ANTIMATTER_BOOTS);
	}

	private static void addTier(
			CreativeModeTab.Output output,
			Item material,
			ModTools.MaterialToolSet tools,
			Item helmet,
			Item chestplate,
			Item leggings,
			Item boots
	) {
		output.accept(material);
		output.accept(tools.sword());
		output.accept(tools.pickaxe());
		output.accept(tools.axe());
		output.accept(tools.shovel());
		output.accept(tools.hoe());
		if (net.kraftattacke.bully.config.BullyConfig.get().features.scythes) output.accept(tools.scythe());
		output.accept(tools.spear());
		output.accept(helmet);
		output.accept(chestplate);
		output.accept(leggings);
		output.accept(boots);
	}
}
