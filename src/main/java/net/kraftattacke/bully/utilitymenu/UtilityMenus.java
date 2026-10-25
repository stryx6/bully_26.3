package net.kraftattacke.bully.utilitymenu;

import net.fabricmc.fabric.api.menu.v1.ExtendedMenuProvider;
import net.fabricmc.fabric.api.menu.v1.ExtendedMenuType;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.CommandSource;
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

public final class UtilityMenus {
    public static final MenuType<UtilityMenu> MAIN = registerType("utility_menu", UtilityMenu.Page.MAIN);
    public static final MenuType<UtilityMenu> COMMAND = registerType("utility_commands", UtilityMenu.Page.COMMAND);

    private UtilityMenus() { }

    private static MenuType<UtilityMenu> registerType(String name, UtilityMenu.Page page) {
        return Registry.register(BuiltInRegistries.MENU, Identifier.fromNamespaceAndPath("bully", name),
                new ExtendedMenuType<>((id, inventory, origin) -> new UtilityMenu(id, inventory, origin, page), BlockPos.STREAM_CODEC));
    }

    public static MenuType<UtilityMenu> type(UtilityMenu.Page page) {
        return switch (page) { case MAIN -> MAIN; case COMMAND -> COMMAND; };
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(UtilityMenuOpenPayload.TYPE, UtilityMenuOpenPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(UtilityMenuPayload.TYPE, UtilityMenuPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(UtilityMenuOpenPayload.TYPE, (payload, context) -> {
            if (!net.kraftattacke.bully.config.BullyConfig.get().utilityMenuEnabled) return;
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
            @Override public Component getDisplayName() { return Component.literal("Utility Menu v4"); }
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
            }
            case COMMAND -> {
                if (button == 0) executeUserCommand(player, command);
                else if (button == 1) open(player, UtilityMenu.Page.MAIN, menu.origin);
            }
        }
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
