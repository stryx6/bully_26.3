package net.kraftattacke.bully.trim;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.gametest.v1.world.TestWorldSave;
import net.fabricmc.fabric.api.client.gametest.v1.screenshot.TestScreenshotOptions;
import net.kraftattacke.bully.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.entity.SignTextSlot;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;

/** Opt-in, reproducible map export. Never runs as part of the ordinary regression tests. */
public final class ArmorTrimArenaGameTest implements FabricClientGameTest {
    private static final String TAG = "bully:trim_arena";
    private static final int FLOOR = -60;
    private static final int EXPECTED = 15 * 23 * 12;
    private static final String[] ARMORS = {"leather", "copper", "chainmail", "iron", "golden", "diamond", "netherite",
            "antimatter", "aetherald", "fluxite", "nebulazul", "mystralite", "voidcrist", "auronyx", "feralyth"};
    private static final String[] ARMOR_NAMES = {"Leder", "Kupfer", "Kettenrüstung", "Eisen", "Gold", "Diamant", "Netherite",
            "Antimatter", "Aetherald", "Fluxite", "Nebulazul", "Mystralite", "Voidcrist", "Auronyx", "Feralyth"};
    private static final String[] PATTERNS = {"bolt", "coast", "crown", "dune", "echo", "eye", "fang", "flow", "host",
            "raiser", "rib", "rift", "sentry", "shaper", "silence", "snout", "spire", "thorn", "tide", "vex", "ward", "wayfinder", "wild"};
    // Front/bottom to back/top: the exact reverse of the requested top-to-bottom list.
    private static final String[] MATERIALS = {"netherite", "amethyst", "lapis", "diamond", "emerald", "gold", "resin",
            "redstone", "rosarium", "quartz", "copper", "iron"};
    private static final String[] MATERIAL_NAMES = {"Netherite", "Amethyst", "Lapis", "Diamant", "Emerald", "Gold", "Resin",
            "Redstone", "Rosarium", "Quarz", "Kupfer", "Eisen"};
    private static final Block[] MARKERS = {Blocks.NETHERITE_BLOCK, Blocks.AMETHYST_BLOCK, Blocks.LAPIS_BLOCK,
            Blocks.DIAMOND_BLOCK, Blocks.EMERALD_BLOCK, Blocks.GOLD_BLOCK, Blocks.RESIN_BLOCK, Blocks.REDSTONE_BLOCK,
            ModBlocks.ROSARIUM_CRYSTAL_BLOCK, Blocks.QUARTZ_BLOCK,
            BuiltInRegistries.BLOCK.getValue(Identifier.fromNamespaceAndPath("minecraft", "waxed_copper_block")), Blocks.IRON_BLOCK};
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final String[] PARTS = {"helmet", "chestplate", "leggings", "boots"};
    private final List<ExpectedStand> expected = new ArrayList<>();
    private final Map<BlockPos, String> expectedSigns = new HashMap<>();
    private final List<Path> screenshots = new ArrayList<>();

    private record ExpectedStand(UUID uuid, int armor, int pattern, int material, Vec3 position, float yaw) { }

    @Override
    public void runTest(ClientGameTestContext context) {
        Path project = Path.of(System.getProperty("bully.arena.projectDir")).toAbsolutePath().normalize();
        TestWorldSave save;
        try (var world = context.worldBuilder().adjustSettings(settings -> settings.setName("Bully Armor Trim Arena")).create()) {
            save = world.getWorldSave();
            world.getServer().runOnServer(server -> {
                validateRegistries(server.overworld());
                forceArena(server.overworld(), true);
                command(world, "time set noon");
                command(world, "weather clear");
                command(world, "defaultgamemode creative");
                command(world, "setworldspawn -110 -59 -83 -90 0");
                buildCourtyard(server.overworld());
            });
            for (int armor = 0; armor < ARMORS.length; armor++) {
                final int index = armor;
                world.getServer().runOnServer(server -> buildTribune(server.overworld(), index));
                context.waitTicks(2);
                System.out.println("ARMOR_ARENA: built " + ARMORS[armor] + " (" + (armor + 1) + "/15)");
            }
            context.waitTicks(40);
            world.getServer().runOnServer(server -> {
                verify(server.overworld());
                verifyInteractions(world.getConnection().getServerPlayer(), server.overworld());
                preparePlayer(world.getConnection().getServerPlayer());
            });
        }

        // A real disconnect/reopen checks that entities, signs and item components survived serialization.
        try (var world = save.open()) {
            context.waitTicks(60);
            world.getServer().waitFor(server -> countStands(server.overworld()) == EXPECTED, 1200);
            world.getServer().runOnServer(server -> verify(server.overworld()));
            photograph(context, world);
            world.getServer().runOnServer(server -> {
                preparePlayer(world.getConnection().getServerPlayer());
                forceArena(server.overworld(), false);
            });
            context.waitTicks(10);
        }
        export(project, save.getSaveDirectory());
        System.out.println("ARMOR_ARENA_CHECK: saved and reopened; all 4140 combinations, 16560 pieces, 345 pattern signs passed");
    }

    private static void command(TestSingleplayerContext world, String text) {
        // Called only from the server thread. The normal command engine handles version-specific world settings.
        world.getServer().runOnServer(server -> {
            try {
                server.getCommands().getDispatcher().execute(text,
                        server.createCommandSourceStack().withSuppressedOutput());
            } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
                throw new AssertionError("command: " + text, e);
            }
        });
    }

    private static Identifier id(String path, boolean custom) {
        return Identifier.fromNamespaceAndPath(custom ? "bully" : "minecraft", path);
    }

    private static boolean customPattern(String name) {
        return Set.of("crown", "echo", "fang", "rift", "thorn").contains(name);
    }

    private static Item armorItem(int armor, int part) {
        Identifier key = id(ARMORS[armor] + "_" + PARTS[part], armor >= 7);
        require(BuiltInRegistries.ITEM.containsKey(key), "missing armor " + key);
        return BuiltInRegistries.ITEM.getValue(key);
    }

    private static ArmorTrim trim(ServerLevel level, int pattern, int material) {
        return new ArmorTrim(level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).getOrThrow(
                ResourceKey.create(Registries.TRIM_MATERIAL, id(MATERIALS[material], MATERIALS[material].equals("rosarium")))),
                level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).getOrThrow(
                        ResourceKey.create(Registries.TRIM_PATTERN, id(PATTERNS[pattern], customPattern(PATTERNS[pattern])))));
    }

    private static void validateRegistries(ServerLevel level) {
        require(level.registryAccess().lookupOrThrow(Registries.TRIM_PATTERN).size() == PATTERNS.length, "unexpected pattern registry");
        require(level.registryAccess().lookupOrThrow(Registries.TRIM_MATERIAL).size() == MATERIALS.length, "unexpected material registry");
        for (int a = 0; a < ARMORS.length; a++) for (int p = 0; p < 4; p++) {
            require(new ItemStack(armorItem(a, p)).is(ItemTags.TRIMMABLE_ARMOR), "untrimmable armor");
        }
        for (int p = 0; p < PATTERNS.length; p++) {
            if (p > 0) require(PATTERNS[p - 1].compareTo(PATTERNS[p]) < 0, "alphabetical pattern order");
            for (int m = 0; m < MATERIALS.length; m++) trim(level, p, m);
        }
    }

    private static void forceArena(ServerLevel level, boolean force) {
        for (int x = -10; x <= 9; x++) for (int z = -10; z <= 9; z++) level.setChunkForced(x, z, force);
    }

    /** Local u increases to the viewer's right, v away from the courtyard. */
    private static BlockPos pos(int side, int u, int y, int v) {
        return switch (side) {
            case 0 -> new BlockPos(u, y, -v);
            case 1 -> new BlockPos(v, y, u);
            case 2 -> new BlockPos(-u, y, v);
            case 3 -> new BlockPos(-v, y, -u);
            default -> throw new IllegalArgumentException("side");
        };
    }

    private static void put(ServerLevel level, int side, int u, int y, int v, Block block) {
        level.setBlock(pos(side, u, y, v), block.defaultBlockState(), 2);
    }

    private void sign(ServerLevel level, int side, int u, int y, int v, String text) {
        BlockPos location = pos(side, u, y, v);
        level.setBlock(location, Blocks.DARK_OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, side * 4), 3);
        require(level.getBlockEntity(location) instanceof SignBlockEntity, "sign block entity");
        var entity = (SignBlockEntity) level.getBlockEntity(location);
        List<Component> lines = List.of(Component.empty(), Component.literal(text), Component.empty(), Component.empty());
        var content = new SignText(lines, lines, DyeColor.WHITE, true);
        entity.setText(content, SignTextSlot.FRONT);
        entity.setText(content, SignTextSlot.BACK);
        entity.setWaxed(true);
        entity.setChanged();
        expectedSigns.put(location, text);
    }

    private void buildCourtyard(ServerLevel level) {
        for (int x = -115; x <= 115; x++) for (int z = -115; z <= 115; z++) {
            if (Math.abs(x) >= 109 || Math.abs(z) >= 109 || Math.abs(x) <= 2 || Math.abs(z) <= 2
                    || (Math.abs(x) <= 10 && Math.abs(z) <= 10) || (x <= -2 && z >= -86 && z <= -80)) {
                level.setBlock(new BlockPos(x, FLOOR, z),
                        (Math.abs(x) % 16 == 0 && Math.abs(z) % 16 == 0 ? Blocks.SEA_LANTERN : Blocks.DEEPSLATE_BRICKS).defaultBlockState(), 2);
            }
        }
        // Fourth west-side bay stays open as an entrance, with a lit approach.
        for (int u = 59; u <= 107; u++) for (int v = 116; v <= 153; v++) {
            put(level, 3, u, FLOOR, v, u % 8 == 3 && v % 8 == 1 ? Blocks.SEA_LANTERN : Blocks.DEEPSLATE_BRICKS);
        }
        sign(level, 3, 80, FLOOR + 1, 118, "Trim-Arena");
        sign(level, 3, 84, FLOOR + 1, 118, "15 Rüstungen");
        sign(level, 3, 88, FLOOR + 1, 118, "23 Muster");
        sign(level, 3, 92, FLOOR + 1, 118, "12 Materialien");
    }

    private void buildTribune(ServerLevel level, int armor) {
        int side = armor / 4;
        int start = -106 + (armor % 4) * 55;
        for (int m = 0; m < MATERIALS.length; m++) {
            int height = FLOOR + m;
            int front = 116 + 3 * m;
            for (int u = 0; u <= 48; u++) for (int v = front; v < front + 3; v++) {
                for (int y = FLOOR; y <= height; y++) {
                    put(level, side, start + u, y, v, (u == 0 || u == 48) ? MARKERS[m] : Blocks.DEEPSLATE_BRICKS);
                }
            }
            // Stair-access aisle alongside the tribune; half steps connect its one-block rises.
            for (int u = 49; u <= 52; u++) for (int v = front; v < front + 3; v++) {
                for (int y = FLOOR; y <= height; y++) put(level, side, start + u, y, v, Blocks.DEEPSLATE_BRICKS);
                if (m > 0 && v == front) level.setBlock(pos(side, start + u, height, v),
                        Blocks.DEEPSLATE_BRICK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.BOTTOM), 2);
            }
            put(level, side, start, height, front, Blocks.SEA_LANTERN);
            put(level, side, start + 48, height, front, Blocks.SEA_LANTERN);
            sign(level, side, start, height + 1, front + 2, MATERIAL_NAMES[m]);

            for (int p = 0; p < PATTERNS.length; p++) {
                BlockPos base = pos(side, start + 2 + 2 * p, height + 1, front + 1);
                var stand = new ArmorStand(level, base.getX() + 0.5, base.getY(), base.getZ() + 0.5);
                stand.addTag(TAG);
                stand.addTag("arena:" + ARMORS[armor] + "/" + PATTERNS[p] + "/" + MATERIALS[m]);
                stand.setYRot(side * 90f);
                stand.setYBodyRot(side * 90f);
                stand.setYHeadRot(side * 90f);
                stand.setNoGravity(true);
                stand.setPermanentlyInvulnerable(true);
                stand.setShowArms(true);
                for (int part = 0; part < SLOTS.length; part++) {
                    var stack = new ItemStack(armorItem(armor, part));
                    stack.set(DataComponents.TRIM, trim(level, p, m));
                    stand.setItemSlot(SLOTS[part], stack);
                }
                require(level.addFreshEntity(stand), "spawn stand");
                expected.add(new ExpectedStand(stand.getUUID(), armor, p, m, stand.position(), side * 90f));
            }
        }
        for (int u = 0; u <= 48; u++) for (int y = FLOOR; y <= FLOOR + 13; y++) {
            put(level, side, start + u, y, 152, Blocks.DEEPSLATE_BRICKS);
        }
        for (int u = 0; u <= 48; u++) for (int v = 111; v <= 115; v++) {
            put(level, side, start + u, FLOOR, v, Blocks.DEEPSLATE_BRICKS);
        }
        for (int p = 0; p < PATTERNS.length; p++) {
            sign(level, side, start + 2 + 2 * p, FLOOR + 1, 114, title(PATTERNS[p]));
        }
        sign(level, side, start + 24, FLOOR + 1, 111, ARMOR_NAMES[armor]);
    }

    private static String title(String name) {
        return name.substring(0, 1).toUpperCase(Locale.ROOT) + name.substring(1);
    }

    private static long countStands(ServerLevel level) {
        long count = 0;
        for (var entity : level.getAllEntities()) if (entity instanceof ArmorStand && entity.entityTags().contains(TAG)) count++;
        return count;
    }

    private void verify(ServerLevel level) {
        require(expected.size() == EXPECTED && countStands(level) == EXPECTED, "4140 stands");
        Set<String> combinations = new HashSet<>();
        for (var entry : expected) {
            require(level.getEntity(entry.uuid) instanceof ArmorStand, "saved UUID " + entry.uuid);
            var stand = (ArmorStand) level.getEntity(entry.uuid);
            require(stand.position().distanceToSqr(entry.position) < 0.000001, "stand position");
            require(Math.abs(Math.IEEEremainder(stand.getYRot() - entry.yaw, 360)) < 0.01, "stand facing");
            require(stand.isPermanentlyInvulnerable() && stand.isNoGravity() && !stand.isMarker(), "stand flags");
            var data = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            stand.saveWithoutId(data);
            require(data.buildResult().getIntOr("DisabledSlots", 0) == 0, "unlocked equipment");
            for (int part = 0; part < SLOTS.length; part++) {
                var stack = stand.getItemBySlot(SLOTS[part]);
                require(stack.is(armorItem(entry.armor, part)), "armor slot");
                var actual = stack.get(DataComponents.TRIM);
                var wanted = trim(level, entry.pattern, entry.material);
                require(actual != null && actual.pattern().equals(wanted.pattern()) && actual.material().equals(wanted.material()), "trim component");
            }
            require(combinations.add(entry.armor + "/" + entry.pattern + "/" + entry.material), "duplicate combination");
        }
        for (var entry : expectedSigns.entrySet()) {
            require(level.getBlockEntity(entry.getKey()) instanceof SignBlockEntity, "saved sign");
            var sign = (SignBlockEntity) level.getBlockEntity(entry.getKey());
            require(sign.getText(SignTextSlot.FRONT).getMessages(false).get(1).getString().equals(entry.getValue()), "sign text");
        }
        require(combinations.size() == EXPECTED && expectedSigns.size() == 15 * (23 + 12 + 1) + 4, "complete manifest");
    }

    private void verifyInteractions(ServerPlayer player, ServerLevel level) {
        var stand = (ArmorStand) level.getEntity(expected.getFirst().uuid);
        Vec3 originalPosition = stand.position();
        for (var mode : new GameType[]{GameType.SURVIVAL, GameType.CREATIVE}) {
            player.setGameMode(mode);
            player.teleportTo(stand.getX(), stand.getY(), stand.getZ() + 2);
            for (int part = 0; part < SLOTS.length; part++) {
                var original = stand.getItemBySlot(SLOTS[part]).copy();
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                stand.interact(player, InteractionHand.MAIN_HAND, new Vec3(0, new double[]{1.8, 1.3, 0.65, 0.1}[part], 0));
                require(stand.getItemBySlot(SLOTS[part]).isEmpty() && player.getMainHandItem().is(original.getItem()), "take " + mode + "/" + part);
                stand.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
                require(ItemStack.isSameItemSameComponents(stand.getItemBySlot(SLOTS[part]), original), "replace " + mode + "/" + part);
                player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            }
            // Swap in a differently trimmed item from a different armor set, then restore the catalogue.
            var original = stand.getItemBySlot(EquipmentSlot.HEAD).copy();
            var mixed = new ItemStack(Items.DIAMOND_HELMET);
            mixed.set(DataComponents.TRIM, trim(level, 4, 8));
            player.setItemInHand(InteractionHand.MAIN_HAND, mixed);
            stand.interact(player, InteractionHand.MAIN_HAND, Vec3.ZERO);
            require(ItemStack.isSameItemSameComponents(stand.getItemBySlot(EquipmentSlot.HEAD), mixed), "custom combination " + mode);
            stand.setItemSlot(EquipmentSlot.HEAD, original);
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            for (int hit = 0; hit < 4; hit++) {
                player.attack(stand);
                stand.hurtServer(level, level.damageSources().playerAttack(player), 1000);
            }
            require(!stand.isRemoved(), "protected against " + mode + " attacks");
            for (var type : MoverType.values()) stand.move(type, new Vec3(1, -1, 1));
            stand.push(1, 1, 1);
            for (int tick = 0; tick < 20; tick++) stand.tick();
            require(stand.position().equals(originalPosition), "anchored " + mode);
        }
        // Unmarked stands must retain normal creative damage behavior.
        var ordinary = new ArmorStand(level, 0, FLOOR + 1, 0);
        level.addFreshEntity(ordinary);
        ordinary.hurtServer(level, level.damageSources().playerAttack(player), 1000);
        require(ordinary.isRemoved(), "ordinary stands remain destructible");
        verify(level);
        System.out.println("ARMOR_ARENA: survival/creative take, replace, mix, attack, movement and ordinary-stand control passed");
    }

    private static void preparePlayer(ServerPlayer player) {
        player.setGameMode(GameType.CREATIVE);
        player.getInventory().clearContent();
        player.teleportTo(player.level(), -110.5, FLOOR + 1, -83.5, Set.of(), -90, 0, true);
        player.getAbilities().flying = false;
        player.onUpdateAbilities();
    }

    private void photograph(ClientGameTestContext context, TestSingleplayerContext world) {
        context.getInput().pressKey(InputConstants.KEY_F1);
        context.runOnClient(client -> {
            client.options.renderDistance().set(16);
            client.options.entityDistanceScaling().set(3.0);
            client.options.broadcastOptions();
        });
        world.getConnection().waitForServerboundPackets();
        for (int side = 0; side < 4; side++) {
            final int s = side;
            world.getServer().runOnServer(server -> {
                var p = world.getConnection().getServerPlayer();
                var camera = pos(s, 0, FLOOR + 50, -30);
                p.teleportTo(server.overworld(), camera.getX() + 0.5, camera.getY(), camera.getZ() + 0.5, Set.of(), s * 90f + 180, 15, true);
                p.getAbilities().flying = true;
                p.onUpdateAbilities();
            });
            context.waitTicks(80);
            context.waitFor(client -> {
                for (int bay = 0; bay < (s == 3 ? 3 : 4); bay++) {
                    if (!client.level.getBlockState(pos(s, -82 + bay * 55, FLOOR + 4, 128)).is(Blocks.DEEPSLATE_BRICKS)) return false;
                }
                return true;
            }, 1200);
            // The selected exhibition chunks must arrive before waiting for their meshes.
            world.getConnection().waitForChunksRender(false, 1200);
            screenshots.add(context.takeScreenshot(TestScreenshotOptions.of("armor-arena-side-" + side).withSize(1600, 900)));
        }
        world.getServer().runOnServer(server -> {
            var p = world.getConnection().getServerPlayer();
            p.teleportTo(server.overworld(), -82, FLOOR + 16, -93, Set.of(), 180, 10, true);
        });
        context.waitTicks(60);
        world.getConnection().waitForChunksRender(false, 1200);
        screenshots.add(context.takeScreenshot(TestScreenshotOptions.of("armor-arena-tribune").withSize(1600, 900)));
        world.getServer().runOnServer(server -> {
            var p = world.getConnection().getServerPlayer();
            p.teleportTo(server.overworld(), -81.5, FLOOR + 3, -109, Set.of(), 180, 12, true);
        });
        context.waitTicks(40);
        world.getConnection().waitForChunksRender(false, 1200);
        screenshots.add(context.takeScreenshot(TestScreenshotOptions.of("armor-arena-pattern-signs").withSize(1600, 900)));
        world.getServer().runOnServer(server -> {
            var p = world.getConnection().getServerPlayer();
            p.teleportTo(server.overworld(), 81.5, FLOOR + 11, 136, Set.of(), 0, 8, true);
        });
        context.waitTicks(40);
        world.getConnection().waitForChunksRender(false, 1200);
        screenshots.add(context.takeScreenshot(TestScreenshotOptions.of("armor-arena-custom-rosarium").withSize(1600, 900)));
        context.getInput().pressKey(InputConstants.KEY_F1);
    }

    private void export(Path project, Path source) {
        Path output = project.resolve("run/saves/Bully Armor Trim Arena");
        Path exports = project.resolve("build/exports");
        Path zip = exports.resolve("Bully Armor Trim Arena.zip");
        try {
            require(!Files.exists(output) && !Files.exists(zip), "export target already exists; preserve it before exporting again");
            Files.createDirectories(exports);
            try (var paths = Files.walk(source)) {
                for (var path : paths.toList()) {
                    Path relative = source.relativize(path);
                    if (path.getFileName().toString().equals("session.lock")) continue;
                    Path target = output.resolve(relative);
                    if (Files.isDirectory(path)) Files.createDirectories(target);
                    else Files.copy(path, target);
                }
            }
            String readme = "Bully Armor Trim Arena\nMinecraft 26.3 / Fabric / Bully 1.5.1 (mit Ausstellungsschutz)\n\n"
                    + "15 Rüstungen, 23 alphabetisch gemischte Vanilla-/Custom-Muster, 12 Materialien.\n"
                    + "4140 Ständer, 16560 Rüstungsteile, 345 Musterschilder.\n"
                    + "Oben nach unten: Eisen, Kupfer, Quarz, Rosarium, Redstone, Resin, Gold, Emerald, Diamant, Lapis, Amethyst, Netherite.\n"
                    + "Rechtsklick: Teile abnehmen und aufsetzen. Linksklick zerstört die markierten Ständer nicht.\n"
                    + "Die Bully-JAR aus diesem Build ist für den Kreativ-Angriffsschutz und die Verankerung erforderlich.\n"
                    + "Ein ZIP entpacken und den enthaltenen Weltordner nach .minecraft/saves kopieren.\n";
            Files.writeString(output.resolve("README.txt"), readme, StandardCharsets.UTF_8);
            try (var stream = new ZipOutputStream(Files.newOutputStream(zip)); var paths = Files.walk(output)) {
                for (var path : paths.filter(Files::isRegularFile).toList()) {
                    stream.putNextEntry(new ZipEntry("Bully Armor Trim Arena/" + output.relativize(path).toString().replace('\\', '/')));
                    Files.copy(path, stream);
                    stream.closeEntry();
                }
            }
            for (var screenshot : screenshots) Files.copy(screenshot, exports.resolve(screenshot.getFileName()));
            Files.writeString(exports.resolve("armor-arena-validation.txt"), readme
                    + "\nPASS: Registry-Vollständigkeit, alphabetische Spalten, 4140 eindeutige Kombinationen und Positionen,\n"
                    + "16560 Trim-Komponenten, alle Schilder, Speicherung und erneutes Laden.\n"
                    + "PASS: Überleben und Kreativ: vier Slots abnehmen/aufsetzen, anderes Set/Muster/Material einsetzen,\n"
                    + "Angriffe, alle MoverTypes, Bewegung über 20 Ticks; unmarkierter Kontrollständer normal zerstörbar.\n"
                    + "Visuelle Prüfung: siehe sieben Screenshots neben dieser Datei.\n", StandardCharsets.UTF_8);
            System.out.println("ARMOR_ARENA: exported " + output + " and " + zip);
        } catch (IOException e) {
            throw new AssertionError("Map export failed", e);
        }
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
