package kraftattacke.bully.net.mixin.client;

import net.kraftattacke.bully.client.PortableClient;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No recipe-book button on a portable crafting table (super.init already ran, so the portable flag is set). */
@Mixin(AbstractRecipeBookScreen.class)
public abstract class AbstractRecipeBookScreenMixin {
	@Inject(method = "initButton", at = @At("HEAD"), cancellable = true)
	private void bully$hideRecipeBook(CallbackInfo ci) {
		AbstractContainerScreen<?> self = (AbstractContainerScreen<?>) (Object) this;
		if (PortableClient.isPortable(self.getMenu().containerId)) {
			ci.cancel();
		}
	}
}
