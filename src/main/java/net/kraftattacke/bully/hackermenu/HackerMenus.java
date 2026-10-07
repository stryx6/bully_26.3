package net.kraftattacke.bully.hackermenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class HackerMenus {
    public static final MenuType<HackerMenu> MAIN = registerType("hacker_menu", HackerMenu.Page.MAIN);
    public static final MenuType<HackerMenu> COMMAND = registerType("hacker_commands", HackerMenu.Page.COMMAND);
    public static final MenuType<HackerMenu> POTION = registerType("hacker_potions", HackerMenu.Page.POTION);

    private HackerMenus() { }

    private static MenuType<HackerMenu> registerType(String name, HackerMenu.Page page) {
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("bully", name),
                new ExtendedMenuType<>((id, inventory, origin) -> new HackerMenu(id, inventory, origin, page), BlockPos.STREAM_CODEC));
    }

    public static MenuType<HackerMenu> type(HackerMenu.Page page) {
        return switch (page) { case MAIN -> MAIN; case COMMAND -> COMMAND; case POTION -> POTION; };
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(HackerMenuOpenPayload.TYPE, HackerMenuOpenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(HackerMenuPayload.TYPE, HackerMenuPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(HackerMenuOpenPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            open(player, HackerMenu.Page.MAIN, player.blockPosition());
        });
        ServerPlayNetworking.registerGlobalReceiver(HackerMenuPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof HackerMenu menu && menu.containerId == payload.containerId()
                    && menu.stillValid(player) && player.level().hasChunkAt(menu.origin)) {
                handle(player, menu, payload.button(), payload.command());
            }
        });
    }

    public static void open(ServerPlayer player, HackerMenu.Page page, BlockPos origin) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override public BlockPos getScreenOpeningData(ServerPlayer player) { return origin; }
            @Override public Component getDisplayName() { return Component.literal("Hacker Menu v4"); }
            @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return new HackerMenu(id, inventory, origin, page);
            }
        });
    }

    private static void handle(ServerPlayer player, HackerMenu menu, int button, String command) {
        switch (menu.page) {
            case MAIN -> {
                switch (button) {
                    case 0 -> player.setGameMode(GameType.SURVIVAL);
                    case 1 -> player.setGameMode(GameType.CREATIVE);
                    case 2 -> player.setGameMode(GameType.SPECTATOR);
                    case 3 -> player.giveExperienceLevels(5);
                    case 4 -> player.giveExperienceLevels(10);
                    case 5 -> player.giveExperienceLevels(50);
                    case 6 -> player.giveExperienceLevels(Integer.MAX_VALUE);
                    case 7 -> menu.duplicate();
                    case 8 -> player.removeAllEffects();
                    case 9 -> effects(player, menu.origin, "fire_resistance", "regeneration", "resistance");
                    case 10 -> open(player, HackerMenu.Page.COMMAND, menu.origin);
                    case 11 -> open(player, HackerMenu.Page.POTION, menu.origin);
                    default -> { }
                }
            }
            case POTION -> {
                switch (button) {
                    case 0 -> effects(player, menu.origin, "haste");
                    case 1 -> effects(player, menu.origin, "saturation");
                    case 2 -> effects(player, menu.origin, "night_vision");
                    case 3 -> effects(player, menu.origin, "invisibility");
                    case 4 -> effects(player, menu.origin, "strength");
                    case 5 -> effects(player, menu.origin, "conduit_power", "dolphins_grace", "water_breathing");
                    case 6 -> player.removeAllEffects();
                    case 7 -> open(player, HackerMenu.Page.MAIN, menu.origin);
                    default -> { }
                }
            }
            case COMMAND -> {
                if (button == 0) executeUserCommand(player, command);
                else if (button == 1) open(player, HackerMenu.Page.MAIN, menu.origin);
            }
        }
    }

    private static void effects(ServerPlayer player, BlockPos origin, String... effects) {
        for (String effect : effects) execute(player, origin, "/effect give @p " + effect + " 1000000 255 true");
    }

    private static void executeUserCommand(ServerPlayer player, String command) {
        String normalized = command.strip();
        if (normalized.startsWith("/")) normalized = normalized.substring(1).stripLeading();
        if (normalized.isEmpty()) return;
        // Deliberately retain full menu privileges, but use the player for @s and relative coordinates.
        var source = player.createCommandSourceStack()
                .withPermission(PermissionSet.ALL_PERMISSIONS)
                .withSource(CommandSource.NULL)
                .withSuppressedOutput();
        player.level().getServer().getCommands().performPrefixedCommand(source, normalized);
    }

    private static void execute(ServerPlayer player, BlockPos origin, String command) {
        var level = player.level();
        // Equivalent to the original anonymous level-4 source: no executor entity, @p at GUI origin.
        var source = new CommandSourceStack(CommandSource.NULL,
                new Vec3(origin.getX(), origin.getY(), origin.getZ()), Vec2.ZERO, level,
                PermissionSet.ALL_PERMISSIONS, Component.empty(), level.getServer()).withSuppressedOutput();
        level.getServer().getCommands().performPrefixedCommand(source, command);
    }
}
