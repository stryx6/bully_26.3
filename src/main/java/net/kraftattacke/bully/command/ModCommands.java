package net.kraftattacke.bully.command;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.config.BullyConfig;
import net.kraftattacke.bully.item.MagicWandItem;
import net.kraftattacke.bully.item.SwordCoating;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

public final class ModCommands {
	private ModCommands() {
	}

	public static void register() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
				dispatcher.register(Commands.literal("bully")
						// COMMANDS_GAMEMASTER is the classic op level 2, like /gamemode.
						.requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
						.then(Commands.literal("reload").executes(ctx -> reload(ctx.getSource())))));
	}

	private static int reload(CommandSourceStack source) {
		try {
			BullyConfig.load();
			SwordCoating.reload();
			MagicWandItem.applyConfig();
		} catch (Exception error) {
			Bully.LOGGER.warn("Failed to reload bully config", error);
			source.sendFailure(Component.literal("Config konnte nicht geladen werden, siehe Log"));
			return 0;
		}
		source.sendSuccess(() -> Component.literal("Bully-Config neu geladen"), true);
		return 1;
	}
}
