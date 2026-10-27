package net.kraftattacke.bully.portable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CartographyTableMenu;

public final class PortableCartographyMenu extends CartographyTableMenu implements PortableStation {
	private final int listPos;

	public PortableCartographyMenu(int id, Inventory inventory, int listPos, int slot) {
		super(id, inventory, PortableStations.access(inventory));
		this.listPos = listPos;
	}

	@Override public int bully$index() { return listPos; }

	@Override public boolean stillValid(Player player) { return true; }
}
