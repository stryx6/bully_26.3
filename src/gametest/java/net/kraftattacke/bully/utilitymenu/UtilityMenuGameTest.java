package net.kraftattacke.bully.utilitymenu;

import java.util.function.Predicate;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.kraftattacke.bully.client.UtilityMenuScreen;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/** Exercises the two remaining features (dupe and command) through real widgets, C2S packets and server mutations. */
public final class UtilityMenuGameTest implements FabricClientGameTest {
    @Override
    public void runTest(ClientGameTestContext context) {
        try (TestSingleplayerContext world = context.worldBuilder().create()) {
            var server = world.getServer();
            var connection = world.getConnection();
            KeyMapping mapping = context.computeOnClient(client -> KeyMapping.get("key.utilitymenu.debugger"));
            context.getInput().pressKey(mapping);
            waitPage(context, UtilityMenu.Page.MAIN);
            context.takeScreenshot("utility-menu-main");

            server.runOnServer(s -> {
                UtilityMenu menu = (UtilityMenu) connection.getServerPlayer().containerMenu;
                require(menu.slots.size() == 45, "nine dupe slots plus player inventory");
                for (int i = 0; i < 9; i++) menu.slots.get(i).set(new ItemStack(Items.DIAMOND, i + 1));
                menu.slots.get(8).set(new ItemStack(Items.DIAMOND, 40));
                menu.broadcastChanges();
            });
            connection.waitForClientboundPackets();
            click(context, world, "gui.utilitymenu.themenugui.button_empty");
            server.runOnServer(s -> {
                UtilityMenu menu = (UtilityMenu) connection.getServerPlayer().containerMenu;
                for (int i = 0; i < 8; i++) require(menu.slots.get(i).getItem().getCount() == 2 * (i + 1), "dupe slot " + i);
                require(menu.slots.get(8).getItem().getCount() == 80, "over-max stack preserved");
            });

            click(context, world, "gui.utilitymenu.themenugui.label_next_page");
            waitPage(context, UtilityMenu.Page.COMMAND);
            context.takeScreenshot("utility-menu-commands");
            context.runOnClient(client -> {
                var screen = client.gui.screen();
                EditBox box = (EditBox) screen.children().stream().filter(child -> child instanceof EditBox).findFirst().orElseThrow();
                screen.setFocused(box);
                box.setFocused(true);
            });
            context.getInput().typeChars("  /give @s minecraft:emerald 3  ");
            context.getInput().pressKey(InputConstants.KEY_RETURN);
            context.getInput().pressKey(InputConstants.KEY_NUMPADENTER);
            connection.waitForServerboundPackets();
            checkPlayer(world, p -> p.getInventory().getNonEquipmentItems().stream()
                    .noneMatch(stack -> stack.is(Items.EMERALD)), "Enter keys must not execute commands");
            click(context, world, "gui.utilitymenu.command_gui.button_execute_com");
            checkPlayer(world, p -> p.getInventory().getNonEquipmentItems().stream()
                    .filter(stack -> stack.is(Items.EMERALD)).mapToInt(ItemStack::getCount).sum() == 3,
                    "command text reached server and executed");
            click(context, world, "gui.utilitymenu.command_gui.label_back");
            waitPage(context, UtilityMenu.Page.MAIN);

            // An action from a stale/closed menu must not mutate player state.
            server.runOnServer(s -> {
                ((UtilityMenu) connection.getServerPlayer().containerMenu).slots.get(0).set(new ItemStack(Items.DIAMOND, 1));
                connection.getServerPlayer().containerMenu.broadcastChanges();
            });
            connection.waitForClientboundPackets();
            context.runOnClient(client -> ClientPlayNetworking.send(new UtilityMenuPayload(-1, 7, "")));
            connection.waitForServerboundPackets();
            server.runOnServer(s -> require(((UtilityMenu) connection.getServerPlayer().containerMenu)
                    .slots.get(0).getItem().getCount() == 1, "stale menu ignored"));
            context.runOnClient(client -> client.player.closeContainer());
            context.waitFor(client -> client.gui.screen() == null);
        }

        // A separate dedicated server proves the command field does not rely on shared client state.
        try (var server = context.worldBuilder().createServer(); var connection = server.connect()) {
            KeyMapping mapping = context.computeOnClient(client -> KeyMapping.get("key.utilitymenu.debugger"));
            context.getInput().pressKey(mapping);
            waitPage(context, UtilityMenu.Page.MAIN);
            server.runOnServer(s -> {
                var player = connection.getServerPlayer();
                s.getPlayerList().deop(player.nameAndId());
                var stack = new ItemStack(Items.DIAMOND, 1);
                stack.set(DataComponents.CUSTOM_NAME, Component.literal("Preserved component"));
                ((UtilityMenu) player.containerMenu).slots.get(0).set(stack);
                player.containerMenu.broadcastChanges();
            });
            connection.waitForClientboundPackets();
            context.clickScreenButton("gui.utilitymenu.themenugui.button_empty");
            connection.waitForServerboundPackets();
            server.runOnServer(s -> {
                var stack = ((UtilityMenu) connection.getServerPlayer().containerMenu).slots.get(0).getItem();
                require(stack.getCount() == 2 && stack.getHoverName().getString().equals("Preserved component"),
                        "dedicated-server dupe retains components");
            });
            context.clickScreenButton("gui.utilitymenu.themenugui.label_next_page");
            waitPage(context, UtilityMenu.Page.COMMAND);
            context.runOnClient(client -> {
                EditBox box = (EditBox) client.gui.screen().children().stream()
                        .filter(child -> child instanceof EditBox).findFirst().orElseThrow();
                box.setValue("give @s minecraft:emerald 7");
            });
            context.clickScreenButton("gui.utilitymenu.command_gui.button_execute_com");
            connection.waitForServerboundPackets();
            server.runOnServer(s -> require(connection.getServerPlayer().getInventory().getNonEquipmentItems().stream()
                    .filter(stack -> stack.is(Items.EMERALD)).mapToInt(ItemStack::getCount).sum() == 7,
                    "non-OP dedicated-server command without leading slash"));
            context.runOnClient(client -> {
                EditBox box = (EditBox) client.gui.screen().children().stream()
                        .filter(child -> child instanceof EditBox).findFirst().orElseThrow();
                box.setValue("save-off");
            });
            context.clickScreenButton("gui.utilitymenu.command_gui.button_execute_com");
            connection.waitForServerboundPackets();
            server.runOnServer(s -> {
                require(!s.getPlayerList().isOp(connection.getServerPlayer().nameAndId()), "player remains non-OP");
                require(connection.getServerPlayer().level().noSave, "non-OP menu executes level-4 save-off");
                for (var level : s.getAllLevels()) level.noSave = false;

                // A second server-side player is present only during this atomic selector check.
                var player = connection.getServerPlayer();
                var other = new ServerPlayer(s, player.level(),
                        new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "OtherMenuPlayer"),
                        net.minecraft.server.level.ClientInformation.createDefault());
                s.getPlayerList().getPlayers().add(other);
                try {
                    var execute = UtilityMenus.class.getDeclaredMethod("executeUserCommand", ServerPlayer.class, String.class);
                    execute.setAccessible(true);
                    execute.invoke(null, player, "give @s minecraft:emerald 2");
                    require(player.getInventory().getNonEquipmentItems().stream()
                            .filter(stack -> stack.is(Items.EMERALD)).mapToInt(ItemStack::getCount).sum() == 9,
                            "@s selects the menu user with two server-side players");
                    require(other.getInventory().getNonEquipmentItems().stream().noneMatch(stack -> stack.is(Items.EMERALD)),
                            "@s does not give items to the other player");
                } catch (ReflectiveOperationException error) {
                    throw new AssertionError("two-player command execution", error);
                } finally {
                    s.getPlayerList().getPlayers().remove(other);
                }
            });
            context.runOnClient(client -> client.player.closeContainer());
            connection.waitForServerboundPackets();
            var chestPosition = server.computeOnServer(s -> {
                var player = connection.getServerPlayer();
                var pos = player.blockPosition().below();
                player.level().setBlockAndUpdate(pos, Blocks.CHEST.defaultBlockState());
                var chest = (ChestBlockEntity) player.level().getBlockEntity(pos);
                chest.setItem(0, new ItemStack(Items.GOLD_INGOT, 3));
                UtilityMenus.open(player, UtilityMenu.Page.MAIN, pos);
                return pos;
            });
            waitPage(context, UtilityMenu.Page.MAIN);
            context.clickScreenButton("gui.utilitymenu.themenugui.button_empty");
            connection.waitForServerboundPackets();
            server.runOnServer(s -> {
                var menu = (UtilityMenu) connection.getServerPlayer().containerMenu;
                var chest = (ChestBlockEntity) connection.getServerPlayer().level().getBlockEntity(menu.origin);
                require(chest.getItem(0).getCount() == 6, "bound chest duplication");
            });
            context.runOnClient(client -> client.player.closeContainer());
            connection.waitForServerboundPackets();
            server.runOnServer(s -> {
                var player = connection.getServerPlayer();
                var chest = (ChestBlockEntity) player.level().getBlockEntity(chestPosition);
                require(chest.getItem(0).getCount() == 6, "closing bound menu keeps chest inventory");
            });
        }
    }

    private static void waitPage(ClientGameTestContext context, UtilityMenu.Page page) {
        context.waitFor(client -> client.gui.screen() instanceof UtilityMenuScreen screen && screen.getMenu().page == page);
    }

    private static void click(ClientGameTestContext context, TestSingleplayerContext world, String key) {
        context.clickScreenButton(key);
        world.getConnection().waitForServerboundPackets();
        world.getConnection().waitForClientboundPackets();
    }

    private static void checkPlayer(TestSingleplayerContext world, Predicate<ServerPlayer> condition, String message) {
        world.getServer().runOnServer(server -> require(condition.test(world.getConnection().getServerPlayer()), message));
    }

    private static void require(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
