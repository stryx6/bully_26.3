package net.kraftattacke.bully.trim;

import java.util.ArrayList;
import java.util.List;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.kraftattacke.bully.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmithingRecipeInput;
import net.minecraft.world.item.crafting.SmithingTransformRecipe;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.item.equipment.trim.TrimMaterial;
import net.minecraft.world.item.equipment.trim.TrimPattern;

public final class TrimGameTest implements FabricClientGameTest {
    private static final String[] PATTERNS = {"echo", "rift", "crown", "fang", "thorn"};

    @Override
    public void runTest(ClientGameTestContext context) {
        try (var world = context.worldBuilder().create()) {
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var level = player.level();
                var materials = level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL);
                var patterns = level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN);
                require(patterns.get(key(Registries.TRIM_PATTERN, "flame")).isEmpty(), "Flame pattern removed");
                require(!BuiltInRegistries.ITEM.containsKey(Identifier.fromNamespaceAndPath("bully", "flame_armor_trim_smithing_template")), "Flame template removed");
                Holder<TrimMaterial> rosarium = materials.getOrThrow(key(Registries.TRIM_MATERIAL, "rosarium"));
                require(materials.get(key(Registries.TRIM_MATERIAL, "antimatter")).isEmpty(), "Antimatter trim material removed");
                require(new ItemStack(ModItems.ROSARIUM_CRYSTAL).get(DataComponents.PROVIDES_TRIM_MATERIAL).is(rosarium.unwrapKey().orElseThrow()), "default trim component");
                require(new ItemStack(ModItems.ANTIMATTER).get(DataComponents.PROVIDES_TRIM_MATERIAL) == null, "Antimatter is not a trim ingredient");
                require(rosarium.value().description().getStyle().getColor().getValue() == 0xF472B6, "pink material tooltip");

                int upgradeChecks = 0;
                for (var holder : level.recipeAccess().getRecipes()) {
                    if (!(holder.value() instanceof SmithingTransformRecipe recipe) || recipe.templateIngredient().isEmpty()) continue;
                    Item template = recipe.templateIngredient().get().items().findFirst().orElseThrow().value();
                    if (template != ModItems.ARMOR_UPGRADE_SMITHING_TEMPLATE && template != ModItems.TOOL_UPGRADE_SMITHING_TEMPLATE) continue;
                    Item addition = recipe.additionIngredient().orElseThrow().items().findFirst().orElseThrow().value();
                    Item base = recipe.baseIngredient().items().findFirst().orElseThrow().value();
                    var input = new SmithingRecipeInput(new ItemStack(template), new ItemStack(base), new ItemStack(addition));
                    Item target = recipe.assemble(input).getItem();
                    var menu = new SmithingMenu(1, player.getInventory());
                    menu.getSlot(0).set(new ItemStack(template));
                    menu.getSlot(1).set(new ItemStack(target));
                    menu.getSlot(2).set(new ItemStack(addition));
                    menu.createResult();
                    require(menu.getSlot(3).getItem().isEmpty(), "self-upgrade blocked: " + target);
                    for (var allowed : recipe.baseIngredient().items().toList()) {
                        ItemStack named = new ItemStack(allowed.value());
                        named.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved upgrade name"));
                        menu.getSlot(1).set(named);
                        menu.createResult();
                        var result = menu.getSlot(3).getItem();
                        require(result.is(target) && result.getHoverName().getString().equals("Preserved upgrade name"), "valid conversion keeps components");
                        upgradeChecks++;
                    }
                }

                List<Item> armors = BuiltInRegistries.ITEM.listElements().filter(h -> h.is(ItemTags.TRIMMABLE_ARMOR)).map(Holder::value).toList();
                List<Item> ingredients = BuiltInRegistries.ITEM.listElements().filter(h -> h.is(ItemTags.TRIM_MATERIALS)).map(Holder::value).toList();
                require(armors.size() >= 61 && ingredients.contains(ModItems.ROSARIUM_CRYSTAL), "all vanilla/mod armors and Rosarium in tags");
                int trimChecks = 0;
                var menu = new SmithingMenu(1, player.getInventory());
                for (String name : PATTERNS) {
                    Item template = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("bully", name + "_armor_trim_smithing_template"));
                    Holder<TrimPattern> pattern = patterns.getOrThrow(key(Registries.TRIM_PATTERN, name));
                    require(pattern.value().copyWithStyle(rosarium).getStyle().getColor().getValue() == 0xF472B6, "pink pattern tooltip");
                    for (Item armor : armors) {
                        for (Item ingredient : ingredients) {
                            menu.getSlot(0).set(new ItemStack(template));
                            menu.getSlot(1).set(new ItemStack(armor));
                            menu.getSlot(2).set(new ItemStack(ingredient));
                            require(menu.getSlot(0).mayPlace(new ItemStack(template)) && menu.getSlot(1).mayPlace(new ItemStack(armor))
                                    && menu.getSlot(2).mayPlace(new ItemStack(ingredient)), "smithing slot filters");
                            menu.createResult();
                            var result = menu.getSlot(3).getItem();
                            var trim = result.get(DataComponents.TRIM);
                            require(result.is(armor) && trim != null && trim.pattern().equals(pattern)
                                    && trim.material().equals(new ItemStack(ingredient).get(DataComponents.PROVIDES_TRIM_MATERIAL)), "trim combination");
                            if (ingredient == ModItems.ROSARIUM_CRYSTAL) {
                                menu.getSlot(1).set(result.copy());
                                menu.createResult();
                                require(menu.getSlot(3).getItem().isEmpty(), "identical trim not reapplied");
                            }
                            trimChecks++;
                        }
                    }
                }
                // Existing vanilla patterns also accept the new crystal through the unchanged vanilla recipe.
                menu.getSlot(0).set(new ItemStack(Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE));
                menu.getSlot(1).set(new ItemStack(ModItems.AETHERALD_CHESTPLATE));
                menu.getSlot(2).set(new ItemStack(ModItems.ROSARIUM_CRYSTAL));
                menu.createResult();
                require(!menu.getSlot(3).getItem().isEmpty(), "vanilla pattern on mod armor with Rosarium");

                Item[] surrounds = {Items.SCULK, Items.OBSIDIAN, Items.GOLD_BLOCK, Items.PACKED_ICE, Items.CACTUS};
                Item[] accents = {Items.ECHO_SHARD, Items.AMETHYST_SHARD, Items.REDSTONE_BLOCK, Items.POINTED_DRIPSTONE, Items.DEAD_BUSH};
                Item[] duplicationBlocks = {Items.CHISELED_DEEPSLATE, Items.OBSIDIAN, Items.GOLD_BLOCK, Items.PACKED_ICE, Items.CACTUS};
                for (int i = 0; i < PATTERNS.length; i++) {
                    var input = CraftingInput.of(3, 3, List.of(new ItemStack(surrounds[i]), new ItemStack(accents[i]), new ItemStack(surrounds[i]),
                            new ItemStack(surrounds[i]), new ItemStack(ModItems.DARK_STAR), new ItemStack(surrounds[i]),
                            new ItemStack(surrounds[i]), new ItemStack(accents[i]), new ItemStack(surrounds[i])));
                    var recipe = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, input, level).orElseThrow();
                    Item template = BuiltInRegistries.ITEM.getValue(Identifier.fromNamespaceAndPath("bully", PATTERNS[i] + "_armor_trim_smithing_template"));
                    var result = recipe.value().assemble(input);
                    require(result.is(template) && result.getCount() == 4, "template crafting: " + PATTERNS[i]);
                    var dupeInput = CraftingInput.of(3, 3, List.of(new ItemStack(Items.DIAMOND), new ItemStack(template), new ItemStack(Items.DIAMOND),
                            new ItemStack(Items.DIAMOND), new ItemStack(duplicationBlocks[i]), new ItemStack(Items.DIAMOND),
                            new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND), new ItemStack(Items.DIAMOND)));
                    var duplicated = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, dupeInput, level).orElseThrow().value().assemble(dupeInput);
                    require(duplicated.is(template) && duplicated.getCount() == 2, "template duplication: " + PATTERNS[i]);
                }
                Item[] crystalIngredients = {Items.GHAST_TEAR, Items.PINK_PETALS, Items.AMETHYST_SHARD};
                for (int a = 0; a < 3; a++) {
                    for (int b = 0; b < 3; b++) {
                        if (a == b) continue;
                        int c = 3 - a - b;
                        var crystalInput = CraftingInput.of(3, 3, List.of(new ItemStack(crystalIngredients[a]), ItemStack.EMPTY, new ItemStack(crystalIngredients[b]),
                                ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY,
                                ItemStack.EMPTY, new ItemStack(crystalIngredients[c]), ItemStack.EMPTY));
                        var result = level.recipeAccess().getRecipeFor(RecipeType.CRAFTING, crystalInput, level).orElseThrow().value().assemble(crystalInput);
                        require(result.is(ModItems.ROSARIUM_CRYSTAL) && result.getCount() == 1, "shapeless crystal arrangement");
                    }
                }
                System.out.println("TRIM TEST PASS: " + upgradeChecks + " conversions; " + trimChecks + " smithing combinations; eleven crafting recipes; six Rosarium arrangements; Flame removed.");
            });

            context.runOnClient(client -> { client.options.guiScale().set(1); client.resizeGui(); });
            context.setScreen(TrimPreviewScreen::new);
            context.waitTicks(5);
            context.takeScreenshot("trim-inventory-color-matrix");
            context.runOnClient(client -> client.gui.setScreen(null));
            world.getServer().runOnServer(server -> {
                var player = world.getConnection().getServerPlayer();
                var material = player.level().registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(key(Registries.TRIM_MATERIAL, "rosarium"));
                var pattern = player.level().registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN, Identifier.withDefaultNamespace("sentry")));
                Item[] items = {ModItems.AETHERALD_HELMET, ModItems.AETHERALD_CHESTPLATE, ModItems.AETHERALD_LEGGINGS, ModItems.AETHERALD_BOOTS};
                EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
                for (int i = 0; i < items.length; i++) {
                    var stack = new ItemStack(items[i]);
                    stack.set(DataComponents.TRIM, new ArmorTrim(material, pattern));
                    player.setItemSlot(slots[i], stack);
                }
            });
            context.waitTicks(10);
            world.getConnection().waitForClientboundPackets();
            context.setScreen(() -> new InventoryScreen(Minecraft.getInstance().player));
            context.waitTicks(5);
            context.takeScreenshot("rosarium-worn-aetherald-armor");
            context.runOnClient(client -> client.gui.setScreen(null));
        }
    }

    private static <T> ResourceKey<T> key(ResourceKey<? extends net.minecraft.core.Registry<T>> registry, String name) {
        return ResourceKey.create(registry, Identifier.fromNamespaceAndPath("bully", name));
    }

    private static void require(boolean condition, String message) { if (!condition) throw new AssertionError(message); }

    private static final class TrimPreviewScreen extends Screen {
        private final List<List<ItemStack>> rows = new ArrayList<>();
        private final String[] names = {"Antimatter", "Aetherald", "Auronyx", "Feralyth", "Fluxite", "Mystralite", "Nebulazul", "Voidcrist",
                "Leather", "Chainmail", "Copper", "Iron", "Gold", "Diamond", "Netherite", "Turtle"};

        private TrimPreviewScreen() {
            super(Component.literal("Trim verification"));
            Item[] items = {ModItems.ANTIMATTER_CHESTPLATE, ModItems.AETHERALD_CHESTPLATE, ModItems.AURONYX_CHESTPLATE,
                    ModItems.FERALYTH_CHESTPLATE, ModItems.FLUXITE_CHESTPLATE, ModItems.MYSTRALITE_CHESTPLATE,
                    ModItems.NEBULAZUL_CHESTPLATE, ModItems.VOIDCRIST_CHESTPLATE,
                    Items.LEATHER_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.COPPER_CHESTPLATE, Items.IRON_CHESTPLATE,
                    Items.GOLDEN_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.NETHERITE_CHESTPLATE, Items.TURTLE_HELMET};
            var registry = minecraft.level.registryAccess();
            var pattern = registry.lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(ResourceKey.create(Registries.TRIM_PATTERN, Identifier.withDefaultNamespace("sentry")));
            for (Item item : items) {
                List<ItemStack> row = new ArrayList<>();
                row.add(new ItemStack(item));
                for (String material : new String[]{"minecraft:diamond", "minecraft:emerald", "bully:rosarium"}) {
                    var stack = new ItemStack(item);
                    stack.set(DataComponents.TRIM, new ArmorTrim(registry.lookupOrThrow(Registries.TRIM_MATERIAL)
                            .getOrThrow(ResourceKey.create(Registries.TRIM_MATERIAL, Identifier.parse(material))), pattern));
                    row.add(stack);
                }
                rows.add(row);
            }
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            graphics.fill(0, 0, width, height, 0xFF202020);
            graphics.text(font, "Armor", 20, 12, -1);
            String[] columns = {"Plain", "Diamond", "Emerald", "Rosarium"};
            for (int x = 0; x < columns.length; x++) graphics.text(font, columns[x], 140 + 90 * x, 12, -1);
            for (int y = 0; y < rows.size(); y++) {
                graphics.text(font, names[y], 20, 36 + 25 * y, -1);
                for (int x = 0; x < 4; x++) graphics.item(rows.get(y).get(x), 150 + 90 * x, 32 + 25 * y);
            }
        }
    }
}
