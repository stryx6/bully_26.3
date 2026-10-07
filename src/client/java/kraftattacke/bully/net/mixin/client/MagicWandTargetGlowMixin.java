package kraftattacke.bully.net.mixin.client;

import net.kraftattacke.bully.client.MagicWandTargeting;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public abstract class MagicWandTargetGlowMixin {
	@Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/client/renderer/entity/state/EntityRenderState;F)V", at = @At("TAIL"))
	private void bully$applyMagicWandTargetGlow(Entity entity, EntityRenderState renderState, float tickProgress, CallbackInfo ci) {
		int outlineColor = MagicWandTargeting.outlineColorFor(entity);
		if (outlineColor != EntityRenderState.NO_OUTLINE) {
			renderState.outlineColor = outlineColor;
		}
	}
}
