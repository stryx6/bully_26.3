package net.kraftattacke.bully.hackermenu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;

public final class HackerMenu extends AbstractContainerMenu {
    public enum Page { MAIN, COMMAND, POTION }

    public final Page page;
    public final BlockPos origin;
    private final Container dupeItems;
    private final BaseContainerBlockEntity boundBlock;

    public HackerMenu(int id, Inventory inventory, BlockPos origin, Page page) {
        super(HackerMenus.type(page), id);
        this.page = page;
        this.origin = origin.immutable();
        // The original can bind its nine slots to a container at the opening position.
        var block = inventory.player.level().getBlockEntity(origin);
        boundBlock = page != Page.POTION && block instanceof BaseContainerBlockEntity container
                && container.getContainerSize() >= 9 ? container : null;
        dupeItems = page == Page.MAIN && boundBlock != null ? boundBlock : new SimpleContainer(9);
        if (page == Page.MAIN) {
            for (int i = 0; i < 9; i++) {
                addSlot(new Slot(dupeItems, i, 123 + (i % 3) * 18, 8 + (i / 3) * 18));
            }
        }
        if (page != Page.POTION) {
            int x = page == Page.MAIN ? 70 : 8;
            int y = page == Page.MAIN ? 87 : 84;
            for (int row = 0; row < 3; row++) {
                for (int column = 0; column < 9; column++) {
                    addSlot(new Slot(inventory, column + (row + 1) * 9, x + column * 18, y + row * 18));
                }
            }
            for (int i = 0; i < 9; i++) {
                addSlot(new Slot(inventory, i, x + i * 18, page == Page.MAIN ? 145 : 142));
            }
        }
    }

    public void duplicate() {
        if (page != Page.MAIN) return;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = dupeItems.getItem(i);
            if (!stack.isEmpty()) {
                // Deliberately retain grow(count), including stacks beyond the normal maximum.
                stack.grow(stack.getCount());
                slots.get(i).setChanged();
            }
        }
        broadcastChanges();
    }

    @Override
    public boolean stillValid(Player player) {
        return boundBlock == null || stillValid(ContainerLevelAccess.create(player.level(), origin),
                player, boundBlock.getBlockState().getBlock());
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        if (index < 0 || index >= slots.size()) return ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (!slot.hasItem()) return ItemStack.EMPTY;
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int custom = page == Page.MAIN ? 9 : 0;
        boolean moved;
        if (index < custom) {
            moved = moveItemStackTo(stack, custom, slots.size(), true);
        } else if (custom > 0 && moveItemStackTo(stack, 0, custom, false)) {
            moved = true;
        } else if (index < custom + 27) {
            moved = moveItemStackTo(stack, custom + 27, slots.size(), true);
        } else {
            moved = moveItemStackTo(stack, custom, custom + 27, false);
        }
        if (!moved) return ItemStack.EMPTY;
        if (stack.isEmpty()) slot.set(ItemStack.EMPTY);
        else slot.setChanged();
        slot.onTake(player, stack);
        return copy;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        if (page == Page.MAIN && boundBlock == null && !player.level().isClientSide()) {
            clearContainer(player, dupeItems);
        }
    }
}
