package net.kraftattacke.bully.portable;

import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.client.gametest.v1.context.TestSingleplayerContext;
import net.kraftattacke.bully.client.PortableClient;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Carried workbenches open in slot order and the arrows cycle through them with wrap-around. */
public final class PortableStationsGameTest implements FabricClientGameTest {
	@Override
	public void runTest(ClientGameTestContext context) {
		try (TestSingleplayerContext world = context.worldBuilder().create()) {
			var server = world.getServer();
			var connection = world.getConnection();

			// Slot order 0,1,2 -> crafting, anvil, grindstone.
			server.runOnServer(s -> {
				var inventory = connection.getServerPlayer().getInventory();
				inventory.setItem(0, new ItemStack(Items.CRAFTING_TABLE));
				inventory.setItem(1, new ItemStack(Items.ANVIL));
				inventory.setItem(2, new ItemStack(Items.GRINDSTONE));
			});

			KeyMapping mapping = context.computeOnClient(client -> KeyMapping.get("key.utilitymenu.utility_menu"));
			context.getInput().pressKey(mapping);
			connection.waitForServerboundPackets();
			server.runOnServer(s -> require(connection.getServerPlayer().containerMenu instanceof PortableCraftingMenu,
					"hotkey opens the first carried station (crafting)"));

			context.runOnClient(client -> PortableClient.sendCycle(1));
			connection.waitForServerboundPackets();
			connection.waitForClientboundPackets();
			server.runOnServer(s -> require(connection.getServerPlayer().containerMenu instanceof PortableAnvilMenu,
					"right cycles to the next slot (anvil)"));

			context.runOnClient(client -> PortableClient.sendCycle(-1));
			connection.waitForServerboundPackets();
			connection.waitForClientboundPackets();
			server.runOnServer(s -> require(connection.getServerPlayer().containerMenu instanceof PortableCraftingMenu,
					"left goes back to crafting"));

			context.runOnClient(client -> PortableClient.sendCycle(-1));
			connection.waitForServerboundPackets();
			connection.waitForClientboundPackets();
			server.runOnServer(s -> {
				AbstractContainerMenu menu = connection.getServerPlayer().containerMenu;
				require(menu instanceof PortableGrindstoneMenu, "left from the first wraps to the last (grindstone)");
			});

			context.runOnClient(client -> client.player.closeContainer());
		}
	}

	private static void require(boolean condition, String message) {
		if (!condition) throw new AssertionError(message);
	}
}
