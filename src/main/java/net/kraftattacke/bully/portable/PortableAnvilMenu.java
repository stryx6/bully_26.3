package net.kraftattacke.bully.portable;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;

public final class PortableAnvilMenu extends AnvilMenu implements PortableStation {
	private final int listPos;
	private final int slot;

	public PortableAnvilMenu(int id, Inventory inventory, int listPos, int slot) {
		super(id, inventory, PortableStations.access(inventory));
		this.listPos = listPos;
		this.slot = slot;
	}

	@Override public int bully$index() { return listPos; }

	@Override public boolean stillValid(Player player) { return true; }

	@Override
	protected void onTake(Player player, ItemStack stack) {
		// Vanilla would wear down the placed block via the (now no-op) access lambda; wear the carried item instead.
		super.onTake(player, stack);
		PortableStations.degradeCarriedAnvil(player, slot);
	}
}
