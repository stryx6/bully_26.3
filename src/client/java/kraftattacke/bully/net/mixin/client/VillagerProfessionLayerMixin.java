package kraftattacke.bully.net.mixin.client;

import net.kraftattacke.bully.Bully;
import net.kraftattacke.bully.villager.ModVillagers;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.layers.VillagerProfessionLayer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.VillagerDataHolderRenderState;
import net.minecraft.resources.Identifier;
import com.mojang.blaze3d.vertex.PoseStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(VillagerProfessionLayer.class)
public abstract class VillagerProfessionLayerMixin {
	@Shadow
	private String path;

	@Redirect(
			method = "submit",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/entity/layers/VillagerProfessionLayer;getIdentifier(Ljava/lang/String;Lnet/minecraft/resources/Identifier;)Lnet/minecraft/resources/Identifier;"
			)
	)
	private Identifier bully$redirectDiscographerLevelTextures(
			VillagerProfessionLayer<?, ?> instance,
			String kind,
			Identifier textureId,
			PoseStack poseStack,
			SubmitNodeCollector submitNodeCollector,
			int packedLight,
			LivingEntityRenderState renderState,
			float limbSwing,
			float partialTick
	) {
		if (!(renderState instanceof VillagerDataHolderRenderState villagerState)) {
			return bully$buildIdentifier(kind, textureId);
		}

		var villagerData = villagerState.getVillagerData();
		if (villagerData != null
				&& "profession_level".equals(kind)
				&& villagerData.profession().is(ModVillagers.DISCOGRAPHER_PROFESSION)) {
			return Identifier.fromNamespaceAndPath(
					Bully.MOD_ID,
					"textures/entity/" + this.path + "/" + kind + "/" + textureId.getPath() + ".png"
			);
		}

		return bully$buildIdentifier(kind, textureId);
	}

	private Identifier bully$buildIdentifier(String kind, Identifier textureId) {
		return textureId.withPath(path -> "textures/entity/" + this.path + "/" + kind + "/" + path + ".png");
	}
}
