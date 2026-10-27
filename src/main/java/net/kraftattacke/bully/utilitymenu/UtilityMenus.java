package net.kraftattacke.bully.utilitymenu;

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
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

public final class UtilityMenus {
    public static final MenuType<UtilityMenu> MAIN = registerType("utility_menu", UtilityMenu.Page.MAIN);
    public static final MenuType<UtilityMenu> COMMAND = registerType("utility_commands", UtilityMenu.Page.COMMAND);
    public static final MenuType<UtilityMenu> POTION = registerType("utility_potions", UtilityMenu.Page.POTION);

    private UtilityMenus() { }

    private static MenuType<UtilityMenu> registerType(String name, UtilityMenu.Page page) {
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("bully", name),
                new ExtendedMenuType<>((id, inventory, origin) -> new UtilityMenu(id, inventory, origin, page), BlockPos.STREAM_CODEC));
    }

    public static MenuType<UtilityMenu> type(UtilityMenu.Page page) {
        return switch (page) { case MAIN -> MAIN; case COMMAND -> COMMAND; case POTION -> POTION; };
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(UtilityMenuOpenPayload.TYPE, UtilityMenuOpenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UtilityMenuPayload.TYPE, UtilityMenuPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UtilityMenuOpenPayload.TYPE, (payload, context) -> {
            if (!net.kraftattacke.bully.config.BullyConfig.get().bullyMenuEnabled) return;
            ServerPlayer player = context.player();
            open(player, UtilityMenu.Page.MAIN, player.blockPosition());
        });
        ServerPlayNetworking.registerGlobalReceiver(UtilityMenuPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (player.containerMenu instanceof UtilityMenu menu && menu.containerId == payload.containerId()
                    && menu.stillValid(player) && player.level().hasChunkAt(menu.origin)) {
                handle(player, menu, payload.button(), payload.command());
            }
        });
    }

    public static void open(ServerPlayer player, UtilityMenu.Page page, BlockPos origin) {
        player.openMenu(new ExtendedMenuProvider<BlockPos>() {
            @Override public BlockPos getScreenOpeningData(ServerPlayer player) { return origin; }
            @Override public Component getDisplayName() { return Component.literal("Bully Menu"); }
            @Override public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
                return new UtilityMenu(id, inventory, origin, page);
            }
        });
    }

    private static void handle(ServerPlayer player, UtilityMenu menu, int button, String command) {
        switch (menu.page) {
            case MAIN -> {
                if (button == 7) menu.duplicate();
                else if (button == 10) open(player, UtilityMenu.Page.COMMAND, menu.origin);
                else if (button == 11) open(player, UtilityMenu.Page.POTION, menu.origin);
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
                    case 7 -> open(player, UtilityMenu.Page.MAIN, menu.origin);
                    case 8 -> effects(player, menu.origin, "fire_resistance", "regeneration", "resistance"); // Semi-God
                    default -> { }
                }
            }
            case COMMAND -> {
                if (button == 0) executeUserCommand(player, command);
                else if (button == 1) open(player, UtilityMenu.Page.MAIN, menu.origin);
            }
        }
    }

    private static void effects(ServerPlayer player, BlockPos origin, String... effects) {
        // infinite duration, level 255 (amplifier 255), hidden particles (true).
        for (String effect : effects) execute(player, origin, "/effect give @p " + effect + " infinite 255 true");
    }

    private static void execute(ServerPlayer player, BlockPos origin, String command) {
        var level = player.level();
        // Level-4 source with no executor entity, @p resolved at the GUI origin (same as the original menu).
        var source = new CommandSourceStack(CommandSource.NULL,
                new Vec3(origin.getX(), origin.getY(), origin.getZ()), Vec2.ZERO, level,
                PermissionSet.ALL_PERMISSIONS, Component.empty(), level.getServer()).withSuppressedOutput();
        level.getServer().getCommands().performPrefixedCommand(source, command);
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
}
