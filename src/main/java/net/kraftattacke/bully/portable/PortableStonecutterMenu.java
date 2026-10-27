package net.kraftattacke.bully.portable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.StonecutterMenu;

public final class PortableStonecutterMenu extends StonecutterMenu implements PortableStation {
	private final int listPos;

	public PortableStonecutterMenu(int id, Inventory inventory, int listPos, int slot) {
		super(id, inventory, PortableStations.access(inventory));
		this.listPos = listPos;
	}

	@Override public int bully$index() { return listPos; }

	@Override public boolean stillValid(Player player) { return true; }
}
