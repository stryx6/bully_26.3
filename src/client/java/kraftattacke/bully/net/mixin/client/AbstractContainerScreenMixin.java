package kraftattacke.bully.net.mixin.client;

import net.kraftattacke.bully.client.PortableClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Portable workbench extras: left/right cycle arrows and highlights of the current / previous / next carried block. */
@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin extends Screen {
	@Shadow protected int leftPos;
	@Shadow protected int topPos;
	@Shadow protected int imageWidth;

	// Highlight colours (ARGB) matched to the arrow label colours: current green, previous gold, next aqua.
	private static final int CURRENT_COLOR = 0x8055FF55;
	private static final int PREV_COLOR = 0x80FFAA00;
	private static final int NEXT_COLOR = 0x8055FFFF;

	private AbstractContainerScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void bully$addCycleArrows(CallbackInfo ci) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		if (!PortableClient.resolve(self.getMenu().containerId)) {
			return;
		}
		addRenderableWidget(Button.builder(Component.literal("<").withStyle(ChatFormatting.GOLD),
				b -> PortableClient.sendCycle(-1)).bounds(leftPos - 20, topPos + 4, 16, 20).build());
		addRenderableWidget(Button.builder(Component.literal(">").withStyle(ChatFormatting.AQUA),
				b -> PortableClient.sendCycle(1)).bounds(leftPos + imageWidth + 4, topPos + 4, 16, 20).build());
	}

	@Inject(method = "extractContents", at = @At("TAIL"))
	private void bully$highlightCarried(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		if (!PortableClient.isPortable(self.getMenu().containerId)) {
			return;
		}
		int current = PortableClient.currentSlot();
		int prev = PortableClient.prevSlot();
		int next = PortableClient.nextSlot();
		if (prev != current) bully$fillSlot(graphics, self, prev, PREV_COLOR);
		if (next != current && next != prev) bully$fillSlot(graphics, self, next, NEXT_COLOR);
		bully$fillSlot(graphics, self, current, CURRENT_COLOR); // current on top so it wins any overlap
	}

	private void bully$fillSlot(GuiGraphicsExtractor graphics, AbstractContainerScreen<?> self, int inventorySlot, int color) {
		if (inventorySlot < 0 || minecraft == null || minecraft.player == null) {
			return;
		}
		var inventory = minecraft.player.getInventory();
		for (Slot slot : self.getMenu().slots) {
			if (slot.container == inventory && slot.getContainerSlot() == inventorySlot) {
				graphics.fill(leftPos + slot.x, topPos + slot.y, leftPos + slot.x + 16, topPos + slot.y + 16, color);
				return;
			}
		}
	}
}
