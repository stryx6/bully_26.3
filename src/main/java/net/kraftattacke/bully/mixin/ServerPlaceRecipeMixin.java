package net.kraftattacke.bully.mixin;

import net.kraftattacke.bully.portable.PortableStation;
import net.minecraft.network.protocol.game.ServerboundPlaceRecipePacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** No recipe-book placement (and thus no JEI/REI "+" transfer) into a portable workbench. */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerPlaceRecipeMixin {
	@Shadow public ServerPlayer player;

	@Inject(method = "handlePlaceRecipe", at = @At("HEAD"), cancellable = true)
	private void bully$blockPortablePlacement(ServerboundPlaceRecipePacket packet, CallbackInfo ci) {
		if (player.containerMenu instanceof PortableStation) {
			ci.cancel();
		}
	}
}
