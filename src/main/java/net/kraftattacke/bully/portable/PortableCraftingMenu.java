package net.kraftattacke.bully.portable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingMenu;

public final class PortableCraftingMenu extends CraftingMenu implements PortableStation {
	private final int listPos;

	public PortableCraftingMenu(int id, Inventory inventory, int listPos, int slot) {
		// Real level access (so recipes compute); stillValid overridden below so the block-at-pos check never closes it.
		super(id, inventory, PortableStations.access(inventory));
		this.listPos = listPos;
	}

	@Override public int bully$index() { return listPos; }

	@Override public boolean stillValid(Player player) { return true; }
}
