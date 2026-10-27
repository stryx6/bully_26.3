package kraftattacke.bully.net.mixin.client;

import net.kraftattacke.bully.client.PortableClient;
import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** While cycling portable workbenches the menu briefly closes; skip the mouse re-grab so the cursor stays on the arrow. */
@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(method = "grabMouse", at = @At("HEAD"), cancellable = true)
	private void bully$keepCursorWhileCycling(CallbackInfo ci) {
		if (PortableClient.isPending()) {
			ci.cancel();
		}
	}
}
